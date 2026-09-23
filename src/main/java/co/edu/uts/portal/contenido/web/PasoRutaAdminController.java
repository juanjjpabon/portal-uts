package co.edu.uts.portal.contenido.web;

import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.service.ImagenInvalida;
import co.edu.uts.portal.contenido.service.ImagenService;
import co.edu.uts.portal.contenido.service.PasoRutaService;
import co.edu.uts.portal.contenido.web.dto.PasoRutaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pasos de una ruta institucional en el panel (reunion con la directora, 22/9/2026).
 * Queda bajo /admin/recursos/**, que SecurityConfig restringe a ADMIN_FUNCIONAL;
 * ademas, cada cambio lo vuelve a exigir PasoRutaService (@PreAuthorize).
 */
@Controller
@RequestMapping("/admin/recursos/rutas/{rutaId}/pasos")
public class PasoRutaAdminController {

    private final PasoRutaService pasoRutaService;

    public PasoRutaAdminController(PasoRutaService pasoRutaService) {
        this.pasoRutaService = pasoRutaService;
    }

    @ModelAttribute("ruta")
    public Recurso ruta(@PathVariable Long rutaId) {
        return pasoRutaService.ruta(rutaId);
    }

    @GetMapping
    public String lista(@PathVariable Long rutaId, Model model) {
        model.addAttribute("pasos", pasoRutaService.listar(rutaId));
        return "admin/recursos/pasos";
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable Long rutaId, Model model) {
        model.addAttribute("form", new PasoRutaForm());
        model.addAttribute("numeroPaso", pasoRutaService.listar(rutaId).size() + 1);
        return "admin/recursos/paso-formulario";
    }

    @PostMapping
    public String crear(@PathVariable Long rutaId, @Valid @ModelAttribute("form") PasoRutaForm form,
                        BindingResult errores, Model model, RedirectAttributes ra) {
        ImagenProcesada imagen = procesarImagen(form, errores);
        if (errores.hasErrors()) {
            model.addAttribute("numeroPaso", pasoRutaService.listar(rutaId).size() + 1);
            return "admin/recursos/paso-formulario";
        }
        pasoRutaService.crear(rutaId, form, imagen);
        ra.addFlashAttribute("ok", "Paso agregado.");
        return volverALista(rutaId);
    }

    @GetMapping("/{pasoId}/editar")
    public String editar(@PathVariable Long rutaId, @PathVariable Long pasoId, Model model) {
        var paso = pasoRutaService.obtener(rutaId, pasoId);
        model.addAttribute("form", PasoRutaForm.de(paso));
        model.addAttribute("numeroPaso", paso.getOrden());
        return "admin/recursos/paso-formulario";
    }

    @PostMapping("/{pasoId}")
    public String actualizar(@PathVariable Long rutaId, @PathVariable Long pasoId,
                             @Valid @ModelAttribute("form") PasoRutaForm form,
                             BindingResult errores, Model model, RedirectAttributes ra) {
        form.setId(pasoId);
        ImagenProcesada imagen = procesarImagen(form, errores);
        if (errores.hasErrors()) {
            model.addAttribute("numeroPaso", pasoRutaService.obtener(rutaId, pasoId).getOrden());
            return "admin/recursos/paso-formulario";
        }
        pasoRutaService.actualizar(rutaId, pasoId, form, imagen);
        ra.addFlashAttribute("ok", "Paso actualizado.");
        return volverALista(rutaId);
    }

    @PostMapping("/{pasoId}/subir")
    public String subir(@PathVariable Long rutaId, @PathVariable Long pasoId) {
        pasoRutaService.mover(rutaId, pasoId, -1);
        return volverALista(rutaId);
    }

    @PostMapping("/{pasoId}/bajar")
    public String bajar(@PathVariable Long rutaId, @PathVariable Long pasoId) {
        pasoRutaService.mover(rutaId, pasoId, 1);
        return volverALista(rutaId);
    }

    @PostMapping("/{pasoId}/eliminar")
    public String eliminar(@PathVariable Long rutaId, @PathVariable Long pasoId, RedirectAttributes ra) {
        pasoRutaService.eliminar(rutaId, pasoId);
        ra.addFlashAttribute("ok", "Paso eliminado.");
        return volverALista(rutaId);
    }

    private static String volverALista(Long rutaId) {
        return "redirect:/admin/recursos/rutas/" + rutaId + "/pasos";
    }

    private static ImagenProcesada procesarImagen(PasoRutaForm form, BindingResult errores) {
        if (form.getImagenArchivo() == null || form.getImagenArchivo().isEmpty()) {
            return null;
        }
        try {
            return ImagenService.procesar(form.getImagenArchivo());
        } catch (ImagenInvalida e) {
            errores.rejectValue("imagenArchivo", "imagen.invalida", e.getMessage());
            return null;
        }
    }
}
