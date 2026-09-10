package co.edu.uts.portal.autoorientacion.web;

import co.edu.uts.portal.parametros.service.ParametroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HU-06: informacion visible para situaciones urgentes. Publica, a un clic desde
 * cualquier resultado de autoorientacion y desde el navbar.
 */
@Controller
public class UrgenciaController {

    private static final String MENSAJE_DEFAULT =
            "Si tu o alguien mas esta en peligro inmediato, comunicate con la linea de emergencias 123 "
                    + "o acude al servicio de urgencias mas cercano.";

    private final ParametroService parametros;

    public UrgenciaController(ParametroService parametros) {
        this.parametros = parametros;
    }

    @GetMapping("/urgencia")
    public String urgencia(Model model) {
        model.addAttribute("mensaje", parametros.valor("urgencia.mensaje", MENSAJE_DEFAULT));
        model.addAttribute("canalUrl", parametros.valor("canal_institucional.url", "#"));
        model.addAttribute("canalEtiqueta",
                parametros.valor("canal_institucional.etiqueta", "Contactar al canal institucional"));
        return "publico/urgencia";
    }
}
