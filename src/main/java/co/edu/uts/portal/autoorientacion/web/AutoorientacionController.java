package co.edu.uts.portal.autoorientacion.web;

import co.edu.uts.portal.autoorientacion.service.AutoorientacionService;
import co.edu.uts.portal.autoorientacion.service.RespuestasIncompletas;
import co.edu.uts.portal.autoorientacion.service.VersionObsoleta;
import co.edu.uts.portal.autoorientacion.web.dto.RespuestasForm;
import co.edu.uts.portal.parametros.service.ParametroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * M03 - autoorientacion publica (HU-08 a HU-13). Todo es publico y nada se guarda:
 * las respuestas viajan en el POST y se descartan al terminar la peticion.
 */
@Controller
@RequestMapping("/autoorientacion")
public class AutoorientacionController {

    private static final String AVISO_PREVIO_DEFAULT =
            "La autoorientación es una herramienta de reflexión personal y anónima. "
                    + "No guarda tus respuestas, no genera un diagnóstico y no reemplaza la atención de un profesional.";

    private final AutoorientacionService servicio;
    private final ParametroService parametros;

    public AutoorientacionController(AutoorientacionService servicio, ParametroService parametros) {
        this.servicio = servicio;
        this.parametros = parametros;
    }

    @GetMapping
    public String landing(Model model) {
        model.addAttribute("temas", servicio.temasDisponibles());
        return "publico/autoorientacion/landing";
    }

    /** HU-08: aviso previo. El formulario no inicia hasta confirmar que se leyo. */
    @GetMapping("/{slug}")
    public String aviso(@PathVariable String slug, Model model) {
        model.addAttribute("tema", servicio.tema(slug));
        model.addAttribute("avisoPrevio",
                parametros.valor("aviso.autoorientacion.previo", AVISO_PREVIO_DEFAULT));
        return "publico/autoorientacion/aviso";
    }

    /** HU-09: formulario en una sola pagina. Requiere haber pasado por el aviso. */
    @GetMapping("/{slug}/responder")
    public String formulario(@PathVariable String slug,
                             @RequestParam(name = "aviso", required = false) String aviso,
                             Model model) {
        if (!"leido".equals(aviso)) {
            return "redirect:/autoorientacion/" + slug;
        }
        var cuestionario = servicio.paraResponder(slug);
        RespuestasForm form = new RespuestasForm();
        form.setNumeroVersion(cuestionario.numeroVersion());
        model.addAttribute("cuestionario", cuestionario);
        model.addAttribute("form", form);
        return "publico/autoorientacion/formulario";
    }

    /** HU-10/11/12: calcula y muestra el resultado. No persiste nada (HU-13). */
    @PostMapping("/{slug}/resultado")
    public String resultado(@PathVariable String slug, @ModelAttribute("form") RespuestasForm form,
                            Model model, RedirectAttributes ra) {
        try {
            var resultado = servicio.calcular(slug, form.getNumeroVersion(), form.getRespuestas());
            model.addAttribute("resultado", resultado);
            model.addAttribute("tema", servicio.tema(slug));
            model.addAttribute("urgenciaMensaje", parametros.valor("urgencia.mensaje", ""));
            model.addAttribute("canalUrl", parametros.valor("canal_institucional.url", "#"));
            model.addAttribute("canalEtiqueta",
                    parametros.valor("canal_institucional.etiqueta", "Contactar al canal institucional"));
            return "publico/autoorientacion/resultado";
        } catch (RespuestasIncompletas e) {
            model.addAttribute("cuestionario", servicio.paraResponder(slug));
            model.addAttribute("faltantes", e.getPreguntasSinResponder());
            model.addAttribute("error", "Responde todas las preguntas para ver tu resultado.");
            return "publico/autoorientacion/formulario";
        } catch (VersionObsoleta e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/autoorientacion/" + slug;
        }
    }
}
