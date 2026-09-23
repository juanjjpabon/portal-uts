package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.service.ImagenInvalida;
import co.edu.uts.portal.contenido.service.ProcesadorImagen;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Imagenes subidas desde el panel (reunion con la directora, 22/9/2026): validacion por
 * contenido, orientacion de fotos de celular, reduccion de tamano y eliminacion de
 * metadatos (ubicacion GPS).
 */
class ProcesadorImagenTest {

    // ------------------------------------------------------------------ utilidades

    private static byte[] codificar(BufferedImage img, String formato) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, formato, out);
        return out.toByteArray();
    }

    /** Imagen con la mitad superior roja y la inferior azul (para saber si quedo "derecha"). */
    private static BufferedImage mitadRojaArriba(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, w, h / 2);
        g.setColor(Color.BLUE);
        g.fillRect(0, h / 2, w, h - h / 2);
        g.dispose();
        return img;
    }

    /**
     * Inserta un segmento APP1 "Exif" con la etiqueta Orientation (0x0112) y un texto
     * de "ubicacion" justo despues del SOI de un JPEG, como hacen las camaras de celular.
     */
    private static byte[] conExif(byte[] jpeg, int orientacion) {
        byte[] marca = "GPS-DE-PRUEBA-7.1N-73.1W".getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream tiff = new ByteArrayOutputStream();
        tiff.writeBytes(new byte[]{'M', 'M', 0, 42, 0, 0, 0, 8});      // cabecera TIFF big-endian, IFD0 en 8
        tiff.writeBytes(new byte[]{0, 1});                              // 1 entrada
        tiff.writeBytes(new byte[]{0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, (byte) orientacion, 0, 0}); // Orientation
        tiff.writeBytes(new byte[]{0, 0, 0, 0});                        // sin siguiente IFD
        tiff.writeBytes(marca);
        byte[] cuerpo = tiff.toByteArray();
        int longitud = 2 + 6 + cuerpo.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xFF);
        out.write(0xD8);
        out.write(0xFF);
        out.write(0xE1);
        out.write((longitud >> 8) & 0xFF);
        out.write(longitud & 0xFF);
        out.writeBytes(new byte[]{'E', 'x', 'i', 'f', 0, 0});
        out.writeBytes(cuerpo);
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

    private static boolean contiene(byte[] datos, byte[] buscado) {
        outer:
        for (int i = 0; i <= datos.length - buscado.length; i++) {
            for (int j = 0; j < buscado.length; j++) {
                if (datos[i + j] != buscado[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private static BufferedImage leer(byte[] datos) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(datos));
    }

    // ---------------------------------------------------------------------- pruebas

    @Test
    void fotoDeCelularSeGiraSegunExifYPierdeLosMetadatos() throws IOException {
        // "Sensor" en vertical: 300x600 con lo rojo a la IZQUIERDA; la etiqueta 6 dice
        // "girar 90 grados a la derecha para verla bien" -> debe quedar 600x300 con lo rojo ARRIBA.
        BufferedImage acostada = new BufferedImage(300, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = acostada.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 150, 600);
        g.setColor(Color.BLUE);
        g.fillRect(150, 0, 150, 600);
        g.dispose();
        byte[] original = conExif(codificar(acostada, "jpg"), 6);
        assertThat(contiene(original, "GPS-DE-PRUEBA".getBytes(StandardCharsets.US_ASCII))).isTrue();

        ImagenProcesada r = ProcesadorImagen.procesar(original, "foto.jpg");

        assertThat(r.tipoContenido()).isEqualTo("image/jpeg");
        assertThat(r.ancho()).isEqualTo(600);
        assertThat(r.alto()).isEqualTo(300);
        BufferedImage resultado = leer(r.datos());
        assertThat(new Color(resultado.getRGB(300, 40)).getRed()).isGreaterThan(200);   // arriba: rojo
        assertThat(new Color(resultado.getRGB(300, 260)).getBlue()).isGreaterThan(200); // abajo: azul
        assertThat(contiene(r.datos(), "GPS-DE-PRUEBA".getBytes(StandardCharsets.US_ASCII))).isFalse();
        assertThat(contiene(r.datos(), "Exif".getBytes(StandardCharsets.US_ASCII))).isFalse();
    }

    @Test
    void jpgGrandeSeReduceA1600EnSuLadoMayor() throws IOException {
        byte[] original = codificar(mitadRojaArriba(3200, 2000), "jpg");

        ImagenProcesada r = ProcesadorImagen.procesar(original, "grande.jpg");

        assertThat(r.ancho()).isEqualTo(1600);
        assertThat(r.alto()).isEqualTo(1000);
        assertThat(leer(r.datos()).getWidth()).isEqualTo(1600);
    }

    @Test
    void pngPequenoSeGuardaTalCual() throws IOException {
        byte[] original = codificar(mitadRojaArriba(400, 300), "png");

        ImagenProcesada r = ProcesadorImagen.procesar(original, "folleto.png");

        assertThat(r.tipoContenido()).isEqualTo("image/png");
        assertThat(r.datos()).isEqualTo(original);
        assertThat(r.ancho()).isEqualTo(400);
        assertThat(r.alto()).isEqualTo(300);
    }

    @Test
    void pngGrandeSeReduceYSigueSiendoPng() throws IOException {
        byte[] original = codificar(mitadRojaArriba(1000, 2400), "png");

        ImagenProcesada r = ProcesadorImagen.procesar(original, "banner.png");

        assertThat(r.tipoContenido()).isEqualTo("image/png");
        assertThat(r.alto()).isEqualTo(1600);
        assertThat(r.ancho()).isEqualTo(667);
    }

    @Test
    void gifSeGuardaTalCual() throws IOException {
        byte[] original = codificar(mitadRojaArriba(120, 80), "gif");

        ImagenProcesada r = ProcesadorImagen.procesar(original, "animado.gif");

        assertThat(r.tipoContenido()).isEqualTo("image/gif");
        assertThat(r.datos()).isEqualTo(original);
        assertThat(r.ancho()).isEqualTo(120);
    }

    @Test
    void webpSeReconocePorSuCabecera() {
        // Cabecera WebP extendida (VP8X) de 640x480: ancho-1 y alto-1 en 24 bits little-endian.
        byte[] webp = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, webp, 0, 4);
        System.arraycopy("WEBPVP8X".getBytes(StandardCharsets.US_ASCII), 0, webp, 8, 8);
        webp[24] = (byte) (639 & 0xFF);
        webp[25] = (byte) (639 >> 8);
        webp[27] = (byte) (479 & 0xFF);
        webp[28] = (byte) (479 >> 8);

        ImagenProcesada r = ProcesadorImagen.procesar(webp, "descargada.webp");

        assertThat(r.tipoContenido()).isEqualTo("image/webp");
        assertThat(r.ancho()).isEqualTo(640);
        assertThat(r.alto()).isEqualTo(480);
    }

    @Test
    void unArchivoQueNoEsImagenSeRechazaAunqueDigaPuntoJpg() {
        byte[] texto = "esto no es una imagen".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> ProcesadorImagen.procesar(texto, "falso.jpg"))
                .isInstanceOf(ImagenInvalida.class)
                .hasMessageContaining("no es una imagen");
    }

    @Test
    void archivoVacioSeRechaza() {
        assertThatThrownBy(() -> ProcesadorImagen.procesar(new byte[0], "vacio.png"))
                .isInstanceOf(ImagenInvalida.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void masDe5MbSeRechazaConMensajeClaro() {
        byte[] pesado = new byte[ProcesadorImagen.MAX_BYTES + 1];
        pesado[0] = (byte) 0xFF;
        pesado[1] = (byte) 0xD8;
        pesado[2] = (byte) 0xFF;

        assertThatThrownBy(() -> ProcesadorImagen.procesar(pesado, "enorme.jpg"))
                .isInstanceOf(ImagenInvalida.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void resolucionExcesivaSeRechazaAntesDeDecodificar() throws IOException {
        // 6000 x 4000 = 24 megapixeles (> 16): se detecta leyendo solo la cabecera.
        BufferedImage enorme = new BufferedImage(6000, 4000, BufferedImage.TYPE_BYTE_BINARY);
        byte[] png = codificar(enorme, "png");

        assertThatThrownBy(() -> ProcesadorImagen.procesar(png, "enorme.png"))
                .isInstanceOf(ImagenInvalida.class)
                .hasMessageContaining("resolución");
    }

    @Test
    void jpegCorruptoSeRechaza() {
        byte[] corrupto = new byte[200];
        Arrays.fill(corrupto, (byte) 0x11);
        corrupto[0] = (byte) 0xFF;
        corrupto[1] = (byte) 0xD8;
        corrupto[2] = (byte) 0xFF;

        assertThatThrownBy(() -> ProcesadorImagen.procesar(corrupto, "roto.jpg"))
                .isInstanceOf(ImagenInvalida.class);
    }

    @Test
    void gifSinCuadrosSeRechazaSinErrorInterno() {
        // Revision previa al despliegue: el lector de GIF lanzaba IndexOutOfBoundsException (500).
        byte[] gif = {'G', 'I', 'F', '8', '9', 'a', 1, 0, 1, 0, 0, 0, 0, 0x3B};

        assertThatThrownBy(() -> ProcesadorImagen.procesar(gif, "vacio.gif"))
                .isInstanceOf(ImagenInvalida.class);
    }

    @Test
    void fotoGrandeSeDecodificaSubmuestreadaYQuedaEn1600() throws IOException {
        // 3600 x 2400: se lee 1 de cada 2 pixeles (1800 x 1200) y luego se reduce a 1600.
        byte[] original = conExif(codificar(mitadRojaArriba(3600, 2400), "jpg"), 1);

        ImagenProcesada r = ProcesadorImagen.procesar(original, "grande.jpg");

        assertThat(r.ancho()).isEqualTo(1600);
        assertThat(r.alto()).isEqualTo(1067);
        BufferedImage resultado = leer(r.datos());
        assertThat(new Color(resultado.getRGB(800, 100)).getRed()).isGreaterThan(200);
        assertThat(new Color(resultado.getRGB(800, 1000)).getBlue()).isGreaterThan(200);
    }
}
