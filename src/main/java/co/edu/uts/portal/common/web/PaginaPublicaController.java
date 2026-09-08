package co.edu.uts.portal.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Marcador de posicion para la portada publica (M01). Se ampliara con HU-01..HU-07.
 */
@Controller
public class PaginaPublicaController {

    @GetMapping("/")
    public String inicio() {
        return "public/inicio";
    }
}
