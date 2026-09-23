package co.edu.uts.portal.contenido.web.publico;

import co.edu.uts.portal.contenido.service.ImagenService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sirve las imagenes de contenidos, rutas y pasos (publico, sin autenticacion).
 *
 * El id es un UUID aleatorio: no se pueden recorrer las imagenes probando numeros.
 * Como una imagen nunca cambia (reemplazarla crea otra con otro id), el navegador
 * puede guardarla en cache por un ano sin volver a pedirla.
 */
@Controller
public class ImagenPublicaController {

    private final ImagenService imagenService;

    public ImagenPublicaController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping("/imagenes/{id}")
    public ResponseEntity<byte[]> imagen(@PathVariable UUID id) {
        return imagenService.obtener(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getTipoContenido()))
                        .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                        .eTag("\"" + img.getId() + "\"")
                        .body(img.getDatos()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
