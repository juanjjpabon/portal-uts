package co.edu.uts.portal.contenido.web;

import co.edu.uts.portal.contenido.service.CategoriaService;
import co.edu.uts.portal.contenido.service.OperacionNoPermitida;
import co.edu.uts.portal.contenido.web.dto.CategoriaForm;
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

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaService categoriaService;

    public CategoriaAdminController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String lista(Model model) {
        var categorias = categoriaService.listar();
        var conteos = categorias.stream().collect(java.util.stream.Collectors.toMap(
                c -> c.getId(), c -> categoriaService.contarRecursos(c.getId())));
        model.addAttribute("categorias", categorias);
        model.addAttribute("conteos", conteos);
        return "admin/categorias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("form", CategoriaForm.nueva());
        return "admin/categorias/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CategoriaForm form,
                        BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            return "admin/categorias/formulario";
        }
        categoriaService.crear(form);
        ra.addFlashAttribute("ok", "Categoria creada.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", CategoriaForm.de(categoriaService.obtener(id)));
        return "admin/categorias/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") CategoriaForm form,
                             BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            return "admin/categorias/formulario";
        }
        categoriaService.actualizar(id, form);
        ra.addFlashAttribute("ok", "Cambios guardados.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoriaService.eliminar(id);
            ra.addFlashAttribute("ok", "Categoria eliminada.");
        } catch (OperacionNoPermitida e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}
