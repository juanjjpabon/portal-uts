package co.edu.uts.portal.cuestionario.web;

import co.edu.uts.portal.cuestionario.service.NivelService;
import co.edu.uts.portal.cuestionario.web.dto.NivelForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * HU-20/HU-12: niveles de resultado, recomendaciones y rutas de una version BORRADOR.
 */
@Controller
@RequestMapping("/admin/cuestionarios/{id}/v/{numero}/niveles")
public class NivelAdminController {

    private final NivelService nivelService;

    public NivelAdminController(NivelService nivelService) {
        this.nivelService = nivelService;
    }

    @PostMapping
    public String crear(@PathVariable Long id, @PathVariable int numero,
                        @Valid @ModelAttribute NivelForm form, BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "Revisa nombre, explicación y puntajes del nivel.");
        } else {
            nivelService.agregar(id, numero, form);
            ra.addFlashAttribute("ok", "Nivel agregado.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}")
    public String actualizar(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                             @Valid @ModelAttribute NivelForm form, BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", "Revisa nombre, explicación y puntajes del nivel.");
        } else {
            nivelService.actualizar(id, numero, nivelId, form);
            ra.addFlashAttribute("ok", "Nivel actualizado.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}/eliminar")
    public String eliminar(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                           RedirectAttributes ra) {
        nivelService.eliminar(id, numero, nivelId);
        ra.addFlashAttribute("ok", "Nivel eliminado.");
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}/recomendaciones")
    public String agregarRecomendacion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                                       @RequestParam String texto, RedirectAttributes ra) {
        if (texto == null || texto.isBlank()) {
            ra.addFlashAttribute("error", "La recomendación no puede estar vacía.");
        } else {
            nivelService.agregarRecomendacion(id, numero, nivelId, texto);
            ra.addFlashAttribute("ok", "Recomendacion agregada.");
        }
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}/recomendaciones/{recomendacionId}")
    public String actualizarRecomendacion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                                          @PathVariable Long recomendacionId, @RequestParam String texto,
                                          @RequestParam(defaultValue = "0") int orden, RedirectAttributes ra) {
        nivelService.actualizarRecomendacion(id, numero, nivelId, recomendacionId, texto, orden);
        ra.addFlashAttribute("ok", "Recomendacion actualizada.");
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}/recomendaciones/{recomendacionId}/eliminar")
    public String eliminarRecomendacion(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                                        @PathVariable Long recomendacionId, RedirectAttributes ra) {
        nivelService.eliminarRecomendacion(id, numero, nivelId, recomendacionId);
        ra.addFlashAttribute("ok", "Recomendacion eliminada.");
        return editor(id, numero);
    }

    @PostMapping("/{nivelId}/rutas")
    public String asignarRutas(@PathVariable Long id, @PathVariable int numero, @PathVariable Long nivelId,
                               @RequestParam(name = "rutaIds", required = false) Set<Long> rutaIds,
                               RedirectAttributes ra) {
        nivelService.asignarRutas(id, numero, nivelId, rutaIds);
        ra.addFlashAttribute("ok", "Rutas del nivel actualizadas.");
        return editor(id, numero);
    }

    private String editor(Long id, int numero) {
        return "redirect:/admin/cuestionarios/" + id + "/v/" + numero;
    }
}
