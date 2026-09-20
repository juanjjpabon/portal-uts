package co.edu.uts.portal.cuestionario.web;

import co.edu.uts.portal.cuestionario.service.PreguntaService;
import co.edu.uts.portal.cuestionario.web.dto.OpcionForm;
import co.edu.uts.portal.cuestionario.web.dto.PreguntaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-20/HU-09: preguntas y opciones de una version BORRADOR. Todas las rutas
 * redirigen al editor de la version.
 */
@Controller
@RequestMapping("/admin/cuestionarios/{id}/v/{numero}/preguntas")
public class PreguntaAdminController {

    private final PreguntaService preguntaService;

    public PreguntaAdminController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @PostMapping
    public String crear(@PathVariable Long id, @PathVariable int numero,
                        @Valid @ModelAttribute PreguntaForm form, BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "El enunciado de la pregunta es obligatorio.");
        } else {
            preguntaService.agregar(id, numero, form);
            ra.addFlashAttribute("ok", "Pregunta agregada.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{preguntaId}")
    public String actualizar(@PathVariable Long id, @PathVariable int numero, @PathVariable Long preguntaId,
                             @Valid @ModelAttribute PreguntaForm form, BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "El enunciado de la pregunta es obligatorio.");
        } else {
            preguntaService.actualizar(id, numero, preguntaId, form);
            ra.addFlashAttribute("ok", "Pregunta actualizada.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{preguntaId}/eliminar")
    public String eliminar(@PathVariable Long id, @PathVariable int numero, @PathVariable Long preguntaId,
                           RedirectAttributes ra) {
        preguntaService.eliminar(id, numero, preguntaId);
        ra.addFlashAttribute("ok", "Pregunta eliminada.");
        return editor(id, numero);
    }

    @PostMapping("/{preguntaId}/opciones")
    public String crearOpcion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long preguntaId,
                              @Valid @ModelAttribute OpcionForm form, BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "Revisa el texto y el valor de la opción.");
        } else {
            preguntaService.agregarOpcion(id, numero, preguntaId, form);
            ra.addFlashAttribute("ok", "Opcion agregada.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{preguntaId}/opciones/{opcionId}")
    public String actualizarOpcion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long preguntaId,
                                   @PathVariable Long opcionId, @Valid @ModelAttribute OpcionForm form,
                                   BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "Revisa el texto y el valor de la opción.");
        } else {
            preguntaService.actualizarOpcion(id, numero, preguntaId, opcionId, form);
            ra.addFlashAttribute("ok", "Opcion actualizada.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{preguntaId}/opciones/{opcionId}/eliminar")
    public String eliminarOpcion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long preguntaId,
                                 @PathVariable Long opcionId, RedirectAttributes ra) {
        preguntaService.eliminarOpcion(id, numero, preguntaId, opcionId);
        ra.addFlashAttribute("ok", "Opcion eliminada.");
        return editor(id, numero);
    }

    private String editor(Long id, int numero) {
        return "redirect:/admin/cuestionarios/" + id + "/v/" + numero;
    }
}
