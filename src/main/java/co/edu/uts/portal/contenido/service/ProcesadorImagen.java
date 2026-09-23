package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.contenido.domain.ImagenProcesada;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Valida y normaliza una imagen subida desde el panel, antes de guardarla.
 *
 * <ul>
 *   <li>Acepta JPG, PNG, GIF y WebP, reconocidos por su contenido (los primeros
 *       bytes del archivo), no por la extension ni por lo que diga el navegador.</li>
 *   <li>Maximo {@value #MAX_BYTES} bytes por archivo y {@value #MAX_PIXELES} pixeles:
 *       la resolucion se revisa ANTES de decodificar, para que una imagen enorme (o
 *       manipulada para declarar millones de pixeles) no agote la memoria del
 *       servidor.</li>
 *   <li>Cuidado de memoria (revision previa al despliegue, 22/9/2026: una sola foto
 *       grande podia agotar el heap en el plan de Render y tumbar la app): las fotos
 *       grandes se decodifican ya submuestreadas (a lo sumo ~{@value #MAX_DECODIFICADO} px
 *       en el lado mayor), se reducen ANTES de girarlas, y solo se procesa una imagen a
 *       la vez en todo el servidor (un doble clic en "Guardar" no duplica el consumo).</li>
 *   <li>JPG: se vuelve a codificar. Eso aplica la orientacion de la foto (las fotos
 *       de celular vienen "acostadas" con una marca EXIF que indica como girarlas),
 *       la reduce a {@value #MAX_LADO} px en su lado mayor si es mas grande, y
 *       elimina todos los metadatos, incluida la ubicacion GPS que suelen traer las
 *       fotos de celular (minimizacion de datos).</li>
 *   <li>PNG: se reduce si pasa de {@value #MAX_LADO} px; si no, se guarda tal cual.</li>
 *   <li>GIF y WebP: se guardan tal cual (el GIF conserva su animacion).</li>
 * </ul>
 *
 * Clase pura, sin base de datos: se prueba de forma aislada (ProcesadorImagenTest).
 */
public final class ProcesadorImagen {

    public static final int MAX_BYTES = 5 * 1024 * 1024;
    public static final int MAX_LADO = 1600;
    public static final long MAX_PIXELES = 16_000_000L;
    /** Lado mayor maximo con el que se decodifica en memoria (luego se reduce a MAX_LADO). */
    static final int MAX_DECODIFICADO = 2 * MAX_LADO;
    private static final float CALIDAD_JPEG = 0.85f;

    /** Una imagen a la vez: el procesamiento es lo unico del portal que usa mucha memoria. */
    private static final Semaphore TURNO = new Semaphore(1, true);

    static {
        // Sin cache en disco: ImageIO si no crea archivos temporales al leer.
        ImageIO.setUseCache(false);
    }

    private ProcesadorImagen() {
    }

    private enum Formato {
        JPEG("image/jpeg"), PNG("image/png"), GIF("image/gif"), WEBP("image/webp");

        final String tipoContenido;

        Formato(String tipoContenido) {
            this.tipoContenido = tipoContenido;
        }
    }

    public static ImagenProcesada procesar(byte[] original, String nombreOriginal) {
        boolean turno;
        try {
            turno = TURNO.tryAcquire(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            turno = false;
        }
        if (!turno) {
            throw new ImagenInvalida("El servidor está procesando otra imagen. Espera unos segundos e inténtalo de nuevo.");
        }
        try {
            return procesarConTurno(original, nombreOriginal);
        } finally {
            TURNO.release();
        }
    }

    private static ImagenProcesada procesarConTurno(byte[] original, String nombreOriginal) {
        if (original == null || original.length == 0) {
            throw new ImagenInvalida("El archivo está vacío. Elige otra imagen.");
        }
        if (original.length > MAX_BYTES) {
            throw new ImagenInvalida(String.format(Locale.ROOT,
                    "La imagen pesa %.1f MB y el máximo es 5 MB. Redúcela (por ejemplo, expórtala de nuevo "
                            + "con menor calidad o menor tamaño) e inténtalo otra vez.",
                    original.length / (1024.0 * 1024.0)));
        }
        Formato formato = detectar(original);
        if (formato == null) {
            throw new ImagenInvalida("El archivo no es una imagen JPG, PNG, GIF o WebP.");
        }
        return switch (formato) {
            case JPEG -> procesarJpeg(original, nombreOriginal);
            case PNG -> procesarPng(original, nombreOriginal);
            case GIF -> procesarGif(original, nombreOriginal);
            case WEBP -> procesarWebp(original, nombreOriginal);
        };
    }

    // ------------------------------------------------------------------ formatos

    private static ImagenProcesada procesarJpeg(byte[] original, String nombre) {
        int[] dim = dimensionesSinDecodificar(original);
        verificarResolucion(dim);
        int orientacion = orientacionExif(original);
        BufferedImage img = decodificar(original, dim);
        img = reducir(img, BufferedImage.TYPE_INT_RGB);   // primero reducir: girar una copia chica
        img = orientar(img, orientacion);
        img = aRgb(img);
        byte[] datos = codificarJpeg(img);
        return new ImagenProcesada(Formato.JPEG.tipoContenido, datos, img.getWidth(), img.getHeight(), nombre);
    }

    private static ImagenProcesada procesarPng(byte[] original, String nombre) {
        int[] dim = dimensionesSinDecodificar(original);
        verificarResolucion(dim);
        BufferedImage img = decodificar(original, dim);
        if (Math.max(dim[0], dim[1]) <= MAX_LADO) {
            return new ImagenProcesada(Formato.PNG.tipoContenido, original, dim[0], dim[1], nombre);
        }
        BufferedImage reducida = reducir(img, BufferedImage.TYPE_INT_ARGB);
        return new ImagenProcesada(Formato.PNG.tipoContenido, codificar(reducida, "png"),
                reducida.getWidth(), reducida.getHeight(), nombre);
    }

    private static ImagenProcesada procesarGif(byte[] original, String nombre) {
        int[] dim = dimensionesSinDecodificar(original);
        verificarResolucion(dim);
        decodificar(original, dim); // valida que realmente se pueda leer (primer cuadro)
        return new ImagenProcesada(Formato.GIF.tipoContenido, original, dim[0], dim[1], nombre);
    }

    private static ImagenProcesada procesarWebp(byte[] original, String nombre) {
        // Java no trae decodificador de WebP: se valida la cabecera y se guarda tal cual;
        // el navegador es quien la muestra.
        int[] dim = dimensionesWebp(original);
        verificarResolucion(dim);
        return new ImagenProcesada(Formato.WEBP.tipoContenido, original, dim[0], dim[1], nombre);
    }

    // ---------------------------------------------------------------- deteccion

    private static Formato detectar(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Formato.JPEG;
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return Formato.PNG;
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return Formato.GIF;
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Formato.WEBP;
        }
        return null;
    }

    private static void verificarResolucion(int[] dim) {
        if (dim[0] <= 0 || dim[1] <= 0) {
            throw new ImagenInvalida("No pudimos leer el tamaño de la imagen. Prueba con otro archivo.");
        }
        if ((long) dim[0] * dim[1] > MAX_PIXELES) {
            throw new ImagenInvalida(String.format(Locale.ROOT,
                    "La imagen tiene demasiada resolución (%d × %d px). Redúcela a menos de 16 megapíxeles "
                            + "(por ejemplo, 4000 × 3000 px) e inténtalo otra vez.", dim[0], dim[1]));
        }
    }

    /** Ancho y alto leyendo solo la cabecera (sin cargar la imagen completa en memoria). */
    private static int[] dimensionesSinDecodificar(byte[] datos) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(iis);
            if (!lectores.hasNext()) {
                throw new ImagenInvalida("No pudimos leer la imagen. Prueba con otro archivo.");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(iis, true, true);
                return new int[]{lector.getWidth(0), lector.getHeight(0)};
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException e) {
            // RuntimeException: p. ej. un GIF sin ningun cuadro (IndexOutOfBoundsException del lector).
            if (e instanceof ImagenInvalida invalida) {
                throw invalida;
            }
            throw new ImagenInvalida("No pudimos leer la imagen. Prueba con otro archivo.");
        }
    }

    /**
     * Decodifica la imagen. Si es muy grande, la lee ya submuestreada (1 de cada N pixeles)
     * para que el lado mayor quede entre MAX_LADO y MAX_DECODIFICADO: asi una foto de 16 MP
     * ocupa en memoria como una de 4 MP.
     */
    private static BufferedImage decodificar(byte[] datos, int[] dim) {
        int ladoMayor = Math.max(dim[0], dim[1]);
        int factor = ladoMayor > MAX_DECODIFICADO ? (int) Math.ceil(ladoMayor / (double) MAX_DECODIFICADO) : 1;
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(iis);
            if (!lectores.hasNext()) {
                throw new ImagenInvalida("No pudimos leer la imagen. Prueba con otro archivo.");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(iis, true, true);
                ImageReadParam param = lector.getDefaultReadParam();
                if (factor > 1) {
                    param.setSourceSubsampling(factor, factor, 0, 0);
                }
                BufferedImage img = lector.read(0, param);
                if (img == null) {
                    throw new ImagenInvalida("No pudimos leer la imagen. Prueba con otro archivo.");
                }
                return img;
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof ImagenInvalida invalida) {
                throw invalida;
            }
            // El caso tipico: un JPG en CMYK (archivos preparados para imprenta), que Java no lee.
            throw new ImagenInvalida("No pudimos leer la imagen. Si es un archivo preparado para imprenta "
                    + "(CMYK), expórtalo de nuevo \"para web\" (RGB) en JPG o PNG e inténtalo otra vez.");
        }
    }

    // ---------------------------------------------------------- EXIF (orientacion)

    /**
     * Valor de la etiqueta EXIF "Orientation" (1 a 8) de un JPEG, o 1 si no tiene.
     * Recorre los segmentos del JPEG hasta el APP1 "Exif" y lee la etiqueta 0x0112 del
     * primer directorio TIFF. Cualquier dato inconsistente se trata como "sin orientacion".
     */
    static int orientacionExif(byte[] jpeg) {
        int i = 2;
        while (i + 4 <= jpeg.length) {
            if ((jpeg[i] & 0xFF) != 0xFF) {
                return 1;
            }
            int marcador = jpeg[i + 1] & 0xFF;
            if (marcador == 0xFF) {        // relleno entre segmentos
                i++;
                continue;
            }
            if (marcador == 0xD8 || marcador == 0x01 || (marcador >= 0xD0 && marcador <= 0xD7)) {
                i += 2;                    // marcadores sin longitud
                continue;
            }
            if (marcador == 0xDA || marcador == 0xD9) {
                return 1;                  // empezo la imagen: ya no hay EXIF
            }
            int longitud = ((jpeg[i + 2] & 0xFF) << 8) | (jpeg[i + 3] & 0xFF);
            if (longitud < 2 || i + 2 + longitud > jpeg.length) {
                return 1;
            }
            if (marcador == 0xE1 && longitud >= 8 && jpeg[i + 4] == 'E' && jpeg[i + 5] == 'x'
                    && jpeg[i + 6] == 'i' && jpeg[i + 7] == 'f' && jpeg[i + 8] == 0 && jpeg[i + 9] == 0) {
                return orientacionTiff(jpeg, i + 10, i + 2 + longitud);
            }
            i += 2 + longitud;
        }
        return 1;
    }

    private static int orientacionTiff(byte[] b, int inicio, int fin) {
        if (inicio + 8 > fin) {
            return 1;
        }
        boolean le;
        if (b[inicio] == 'I' && b[inicio + 1] == 'I') {
            le = true;
        } else if (b[inicio] == 'M' && b[inicio + 1] == 'M') {
            le = false;
        } else {
            return 1;
        }
        if (u16(b, inicio + 2, le) != 42) {
            return 1;
        }
        long desplazamiento = u32(b, inicio + 4, le);
        if (desplazamiento < 8 || inicio + desplazamiento + 2 > fin) {
            return 1;
        }
        int ifd = inicio + (int) desplazamiento;
        int entradas = u16(b, ifd, le);
        for (int k = 0; k < entradas; k++) {
            int e = ifd + 2 + k * 12;
            if (e + 12 > fin) {
                return 1;
            }
            if (u16(b, e, le) == 0x0112) {
                int valor = u16(b, e + 2, le) == 3 ? u16(b, e + 8, le) : 1; // tipo 3 = SHORT
                return valor >= 1 && valor <= 8 ? valor : 1;
            }
        }
        return 1;
    }

    private static int u16(byte[] b, int i, boolean le) {
        return le ? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8)
                : ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
    }

    private static long u32(byte[] b, int i, boolean le) {
        return le
                ? (b[i] & 0xFFL) | ((b[i + 1] & 0xFFL) << 8) | ((b[i + 2] & 0xFFL) << 16) | ((b[i + 3] & 0xFFL) << 24)
                : ((b[i] & 0xFFL) << 24) | ((b[i + 1] & 0xFFL) << 16) | ((b[i + 2] & 0xFFL) << 8) | (b[i + 3] & 0xFFL);
    }

    /** Aplica la orientacion EXIF (1 a 8) para que la imagen quede "derecha". */
    static BufferedImage orientar(BufferedImage src, int orientacion) {
        if (orientacion <= 1 || orientacion > 8) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        boolean intercambia = orientacion >= 5; // 5 a 8 giran 90 grados: ancho y alto se intercambian
        // Matrices (m00, m10, m01, m11, m02, m12): x' = m00*x + m01*y + m02 ; y' = m10*x + m11*y + m12
        AffineTransform t = switch (orientacion) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, w, 0);   // espejo horizontal
            case 3 -> new AffineTransform(-1, 0, 0, -1, w, h);  // 180 grados
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, h);   // espejo vertical
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);    // transpuesta
            case 6 -> new AffineTransform(0, 1, -1, 0, h, 0);   // 90 grados a la derecha
            case 7 -> new AffineTransform(0, -1, -1, 0, h, w);  // transversa
            default -> new AffineTransform(0, -1, 1, 0, 0, w);  // 8: 90 grados a la izquierda
        };
        int tipo = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage destino = new BufferedImage(intercambia ? h : w, intercambia ? w : h, tipo);
        Graphics2D g = destino.createGraphics();
        try {
            g.drawImage(src, t, null);
        } finally {
            g.dispose();
        }
        return destino;
    }

    // --------------------------------------------------------- tamano y color

    /** Reduce a MAX_LADO en el lado mayor (en mitades sucesivas, mejor calidad que un solo salto). */
    static BufferedImage reducir(BufferedImage src, int tipo) {
        int w = src.getWidth();
        int h = src.getHeight();
        int lado = Math.max(w, h);
        if (lado <= MAX_LADO) {
            return src;
        }
        double factor = (double) MAX_LADO / lado;
        int anchoFinal = Math.max(1, (int) Math.round(w * factor));
        int altoFinal = Math.max(1, (int) Math.round(h * factor));
        BufferedImage actual = src;
        int cw = w;
        int ch = h;
        while (cw / 2 >= anchoFinal && ch / 2 >= altoFinal) {
            cw /= 2;
            ch /= 2;
            actual = escalar(actual, cw, ch, tipo, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        return escalar(actual, anchoFinal, altoFinal, tipo, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private static BufferedImage escalar(BufferedImage src, int w, int h, int tipo, Object interpolacion) {
        BufferedImage destino = new BufferedImage(w, h, tipo);
        Graphics2D g = destino.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolacion);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return destino;
    }

    /** JPG no admite transparencia: se aplana sobre fondo blanco. */
    private static BufferedImage aRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage destino = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, src.getWidth(), src.getHeight());
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return destino;
    }

    private static byte[] codificarJpeg(BufferedImage img) {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(salida)) {
            ImageWriteParam param = escritor.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(CALIDAD_JPEG);
            param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT); // carga progresiva en conexiones lentas
            escritor.setOutput(ios);
            escritor.write(null, new IIOImage(img, null, null), param);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            escritor.dispose();
        }
        return salida.toByteArray();
    }

    private static byte[] codificar(BufferedImage img, String formato) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formato, salida);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return salida.toByteArray();
    }

    // -------------------------------------------------------------------- WebP

    /** Ancho y alto desde la cabecera WebP (formatos VP8, VP8L y VP8X). */
    static int[] dimensionesWebp(byte[] b) {
        if (b.length < 30) {
            throw new ImagenInvalida("El archivo WebP está incompleto. Prueba con otro archivo.");
        }
        String bloque = new String(b, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        switch (bloque) {
            case "VP8X" -> {
                int w = 1 + ((b[24] & 0xFF) | ((b[25] & 0xFF) << 8) | ((b[26] & 0xFF) << 16));
                int h = 1 + ((b[27] & 0xFF) | ((b[28] & 0xFF) << 8) | ((b[29] & 0xFF) << 16));
                return new int[]{w, h};
            }
            case "VP8 " -> {
                if ((b[23] & 0xFF) != 0x9D || (b[24] & 0xFF) != 0x01 || (b[25] & 0xFF) != 0x2A) {
                    break;
                }
                int w = ((b[26] & 0xFF) | ((b[27] & 0xFF) << 8)) & 0x3FFF;
                int h = ((b[28] & 0xFF) | ((b[29] & 0xFF) << 8)) & 0x3FFF;
                return new int[]{w, h};
            }
            case "VP8L" -> {
                if ((b[20] & 0xFF) != 0x2F) {
                    break;
                }
                long bits = (b[21] & 0xFFL) | ((b[22] & 0xFFL) << 8) | ((b[23] & 0xFFL) << 16) | ((b[24] & 0xFFL) << 24);
                int w = (int) (bits & 0x3FFF) + 1;
                int h = (int) ((bits >> 14) & 0x3FFF) + 1;
                return new int[]{w, h};
            }
            default -> {
            }
        }
        throw new ImagenInvalida("No pudimos leer el archivo WebP. Prueba con un JPG o PNG.");
    }
}
