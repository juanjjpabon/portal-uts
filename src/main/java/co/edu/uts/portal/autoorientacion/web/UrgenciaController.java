package co.edu.uts.portal.autoorientacion.web;

import co.edu.uts.portal.contenido.domain.CanalDirecto;
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
            "Si tú o alguien más está en peligro inmediato, comunícate con la línea de emergencias 123 "
                    + "o acude al servicio de urgencias más cercano.";

    private final ParametroService parametros;

    public UrgenciaController(ParametroService parametros) {
        this.parametros = parametros;
    }

    @GetMapping("/urgencia")
    public String urgencia(Model model) {
        model.addAttribute("mensaje", parametros.valor("urgencia.mensaje", MENSAJE_DEFAULT));
        String canalUrl = parametros.valor("canal_institucional.url", "#");
        model.addAttribute("canalUrl", canalUrl);
        // Si el canal es un correo o telefono, se muestra el dato con boton de copiar en vez
        // de un enlace que "no lleva a nada" sin programa de correo (hallazgo 22/9/2026).
        model.addAttribute("canalDirecto", CanalDirecto.desde(canalUrl).orElse(null));
        model.addAttribute("canalEtiqueta",
                parametros.valor("canal_institucional.etiqueta", "Contactar al canal institucional"));
        return "publico/urgencia";
    }
}
