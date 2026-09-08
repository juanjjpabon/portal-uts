package co.edu.uts.portal.parametros.web;

import co.edu.uts.portal.parametros.service.ParametroService;
import co.edu.uts.portal.parametros.web.dto.ParametrosForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-27: edicion de parametros operativos (avisos, urgencia, canal institucional).
 * Catalogo fijo; el administrador funcional solo cambia los valores.
 */
@Controller
@RequestMapping("/admin/parametros")
public class ParametroAdminController {

    private final ParametroService parametroService;

    public ParametroAdminController(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("grupos", parametroService.agrupados());
        return "admin/parametros/lista";
    }

    @PostMapping
    public String guardar(@ModelAttribute ParametrosForm form, RedirectAttributes ra) {
        int cambios = parametroService.guardar(form.getValores());
        ra.addFlashAttribute("ok", cambios == 0
                ? "Sin cambios."
                : cambios + (cambios == 1 ? " parametro actualizado." : " parametros actualizados."));
        return "redirect:/admin/parametros";
    }
}
