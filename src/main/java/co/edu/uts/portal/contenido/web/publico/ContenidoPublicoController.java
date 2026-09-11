package co.edu.uts.portal.contenido.web.publico;

import co.edu.uts.portal.contenido.service.PortalPublicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** HU-01, HU-02, HU-04: contenidos publicados, sin autenticacion (permitAll). */
@Controller
public class ContenidoPublicoController {

    private final PortalPublicoService servicio;

    public ContenidoPublicoController(PortalPublicoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/contenidos")
    public String lista(@RequestParam(required = false) String categoria, Model model) {
        model.addAttribute("contenidos", servicio.contenidos(categoria));
        model.addAttribute("categorias", servicio.categorias());
        model.addAttribute("categoriaActiva", categoria);
        return "publico/contenidos/lista";
    }

    @GetMapping("/contenidos/{slug}")
    public String detalle(@PathVariable String slug, Model model) {
        model.addAttribute("contenido", servicio.contenido(slug));
        return "publico/contenidos/detalle";
    }
}
