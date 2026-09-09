package co.edu.uts.portal.cuestionario.web;

import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import co.edu.uts.portal.cuestionario.web.dto.CuestionarioForm;
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
 * HU-20: cuestionarios y ciclo de versiones (crear, nueva version, publicar, archivar).
 */
@Controller
@RequestMapping("/admin/cuestionarios")
public class CuestionarioAdminController {

    private final CuestionarioService cuestionarioService;

    public CuestionarioAdminController(CuestionarioService cuestionarioService) {
        this.cuestionarioService = cuestionarioService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("cuestionarios", cuestionarioService.listar());
        return "admin/cuestionarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", CuestionarioForm.nuevo());
        return "admin/cuestionarios/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CuestionarioForm form,
                        BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            return "admin/cuestionarios/formulario";
        }
        var c = cuestionarioService.crear(form);
        ra.addFlashAttribute("ok", "Cuestionario creado. Ahora edita su version 1.");
        return "redirect:/admin/cuestionarios/" + c.getId() + "/v/1";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", CuestionarioForm.de(cuestionarioService.obtener(id)));
        return "admin/cuestionarios/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") CuestionarioForm form,
                             BindingResult errores, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            return "admin/cuestionarios/formulario";
        }
        cuestionarioService.actualizarDatos(id, form);
        ra.addFlashAttribute("ok", "Cambios guardados.");
        return "redirect:/admin/cuestionarios/" + id;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        var c = cuestionarioService.obtenerConVersiones(id);
        var versionesDesc = c.getVersiones().stream()
                .sorted((a, b) -> Integer.compare(b.getNumero(), a.getNumero()))
                .toList();
        model.addAttribute("cuestionario", c);
        model.addAttribute("versiones", versionesDesc);
        return "admin/cuestionarios/detalle";
    }

    @PostMapping("/{id}/versiones")
    public String nuevaVersion(@PathVariable Long id, RedirectAttributes ra) {
        int numero = cuestionarioService.crearNuevaVersion(id);
        ra.addFlashAttribute("ok", "Version " + numero + " creada como copia de la anterior.");
        return "redirect:/admin/cuestionarios/" + id + "/v/" + numero;
    }

    @PostMapping("/{id}/v/{numero}/publicar")
    public String publicar(@PathVariable Long id, @PathVariable int numero, RedirectAttributes ra) {
        cuestionarioService.publicar(id, numero);
        ra.addFlashAttribute("ok", "Version " + numero + " publicada.");
        return "redirect:/admin/cuestionarios/" + id;
    }

    @PostMapping("/{id}/v/{numero}/archivar")
    public String archivar(@PathVariable Long id, @PathVariable int numero, RedirectAttributes ra) {
        cuestionarioService.archivar(id, numero);
        ra.addFlashAttribute("ok", "Version " + numero + " archivada.");
        return "redirect:/admin/cuestionarios/" + id;
    }
}
