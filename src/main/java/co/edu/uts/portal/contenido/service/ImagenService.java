package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.contenido.domain.Imagen;
import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.repository.ImagenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Imagenes de contenidos, rutas y pasos (reunion con la directora, 22/9/2026).
 *
 * El procesamiento (validar, orientar, reducir, quitar metadatos) lo hace
 * {@link ProcesadorImagen} antes de guardar, fuera de la transaccion; aqui solo se
 * persiste, se sirve y se limpia. Las escrituras las llaman RecursoService y
 * PasoRutaService dentro de sus propias transacciones (ya protegidas por rol).
 */
@Service
public class ImagenService {

    private final ImagenRepository imagenRepository;

    public ImagenService(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    /**
     * Lee y procesa el archivo subido. Null si no se eligio archivo.
     *
     * @throws ImagenInvalida con un mensaje listo para mostrar al administrador.
     */
    public static ImagenProcesada procesar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        if (archivo.getSize() > ProcesadorImagen.MAX_BYTES) {
            // Se revisa antes de leer los bytes a memoria.
            throw new ImagenInvalida(String.format(java.util.Locale.ROOT,
                    "La imagen pesa %.1f MB y el máximo es 5 MB. Redúcela e inténtalo otra vez.",
                    archivo.getSize() / (1024.0 * 1024.0)));
        }
        try {
            return ProcesadorImagen.procesar(archivo.getBytes(), archivo.getOriginalFilename());
        } catch (IOException e) {
            throw new ImagenInvalida("No se pudo recibir el archivo. Inténtalo de nuevo.");
        }
    }

    @Transactional
    public Imagen guardar(ImagenProcesada procesada) {
        return imagenRepository.save(new Imagen(procesada));
    }

    @Transactional(readOnly = true)
    public Optional<Imagen> obtener(UUID id) {
        return imagenRepository.findById(id);
    }

    /** Borra las imagenes que ya nadie usa. Devuelve cuantas se borraron. */
    @Transactional
    public int eliminarHuerfanas() {
        return imagenRepository.eliminarHuerfanas();
    }
}
