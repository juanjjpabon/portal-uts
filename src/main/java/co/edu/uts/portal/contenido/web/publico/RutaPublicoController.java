package co.edu.uts.portal.contenido.web.publico;

import co.edu.uts.portal.contenido.service.PortalPublicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** HU-05: rutas institucionales y contactos publicados, sin autenticacion. */
@Controller
public class RutaPublicoController {

    private final PortalPublicoService servicio;

    public RutaPublicoController(PortalPublicoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/rutas")
    public String lista(@RequestParam(required = false) String gracias, Model model) {
        model.addAttribute("rutas", servicio.rutas());
        model.addAttribute("contactos", servicio.contactos());
        model.addAttribute("gracias", gracias != null);
        return "publico/rutas/lista";
    }
}
