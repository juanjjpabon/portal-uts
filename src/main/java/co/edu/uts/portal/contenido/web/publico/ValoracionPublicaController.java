package co.edu.uts.portal.contenido.web.publico;

import co.edu.uts.portal.contenido.service.ValoracionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * HU-25: "¿te fue util?" para un contenido o una ruta/contacto. Publica, sin sesion.
 * Vuelve a la pagina de origen leyendo el Referer y validandolo contra una lista
 * blanca de rutas propias -- nunca se redirige a la URL cruda del encabezado, asi
 * que no hay riesgo de open-redirect.
 */
@Controller
public class ValoracionPublicaController {

    private final ValoracionService valoracionService;

    public ValoracionPublicaController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    @PostMapping("/valoraciones/{recursoId}")
    public String votar(@PathVariable Long recursoId, @RequestParam boolean util, HttpServletRequest request) {
        valoracionService.registrar(recursoId, util);
        return "redirect:" + destinoSeguro(request.getHeader("Referer")) + "?gracias=1";
    }

    private String destinoSeguro(String referer) {
        if (referer != null) {
            try {
                String path = URI.create(referer).getPath();
                if (path != null && path.startsWith("/contenidos/") && path.length() > "/contenidos/".length()) {
                    return path;
                }
                if ("/rutas".equals(path)) {
                    return "/rutas";
                }
            } catch (IllegalArgumentException ignored) {
                // Referer mal formado: se ignora y se usa el destino por defecto.
            }
        }
        return "/";
    }
}
