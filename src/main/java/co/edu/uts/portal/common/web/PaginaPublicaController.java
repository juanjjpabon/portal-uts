package co.edu.uts.portal.common.web;

import co.edu.uts.portal.contenido.service.PortalPublicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Portada publica (M01). Muestra los ultimos contenidos publicados para que se
 * abran directamente desde aqui, sin autenticarse (HU-01).
 */
@Controller
public class PaginaPublicaController {

    private final PortalPublicoService portalPublicoService;

    public PaginaPublicaController(PortalPublicoService portalPublicoService) {
        this.portalPublicoService = portalPublicoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("ultimosContenidos", portalPublicoService.ultimosContenidos(3));
        return "public/inicio";
    }
}
