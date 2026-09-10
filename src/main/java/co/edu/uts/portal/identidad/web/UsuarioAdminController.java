package co.edu.uts.portal.identidad.web;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.service.OperacionInvalida;
import co.edu.uts.portal.identidad.service.UsuarioService;
import co.edu.uts.portal.identidad.web.dto.CrearUsuarioForm;
import co.edu.uts.portal.identidad.web.dto.EditarUsuarioForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

/**
 * HU-21: gestion de usuarios y roles. Solo ADMIN_TECNICO (SecurityConfig + el
 * @PreAuthorize de UsuarioService). Las reglas de negocio y la auditoria viven en
 * el servicio; aqui solo se traducen los errores a mensajes flash.
 */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;
    private final BitacoraService bitacoraService;

    public UsuarioAdminController(UsuarioService usuarioService, BitacoraService bitacoraService) {
        this.usuarioService = usuarioService;
        this.bitacoraService = bitacoraService;
    }

    @ModelAttribute("todosLosRoles")
    public NombreRol[] todosLosRoles() {
        return NombreRol.values();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new CrearUsuarioForm());
        return "admin/usuarios/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CrearUsuarioForm form, BindingResult errores,
                        RedirectAttributes ra) {
        if (errores.hasErrors()) {
            return "admin/usuarios/formulario";
        }
        try {
            var u = usuarioService.crear(form);
            ra.addFlashAttribute("ok", "Usuario creado. Debera cambiar la contrasena al ingresar.");
            return "redirect:/admin/usuarios/" + u.getId();
        } catch (OperacionInvalida e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios/nuevo";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        var u = usuarioService.obtener(id);
        model.addAttribute("usuario", u);
        model.addAttribute("rolesUsuario", u.getRoles().stream().map(r -> r.getNombre().name()).toList());
        model.addAttribute("esUsuarioActual", principal != null && id.equals(principal.getId()));
        model.addAttribute("historial", bitacoraService.historial("Usuario", id));
        return "admin/usuarios/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", EditarUsuarioForm.de(usuarioService.obtener(id)));
        model.addAttribute("usuarioId", id);
        return "admin/usuarios/editar";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") EditarUsuarioForm form,
                             BindingResult errores, Model model, RedirectAttributes ra) {
        if (errores.hasErrors()) {
            model.addAttribute("usuarioId", id);
            return "admin/usuarios/editar";
        }
        try {
            usuarioService.actualizarDatos(id, form);
            ra.addFlashAttribute("ok", "Datos actualizados.");
        } catch (OperacionInvalida e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios/" + id;
    }

    @PostMapping("/{id}/roles")
    public String roles(@PathVariable Long id,
                        @RequestParam(name = "roles", required = false) Set<NombreRol> roles,
                        RedirectAttributes ra) {
        try {
            usuarioService.fijarRoles(id, roles == null ? new HashSet<>() : roles);
            ra.addFlashAttribute("ok", "Roles actualizados.");
        } catch (OperacionInvalida e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios/" + id;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        cambiarEstado(id, true, ra);
        return "redirect:/admin/usuarios/" + id;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        cambiarEstado(id, false, ra);
        return "redirect:/admin/usuarios/" + id;
    }

    @PostMapping("/{id}/contrasena")
    public String restablecerContrasena(@PathVariable Long id, @RequestParam String contrasena,
                                        RedirectAttributes ra) {
        if (contrasena == null || contrasena.length() < 8) {
            ra.addFlashAttribute("error", "La contrasena debe tener al menos 8 caracteres.");
        } else {
            usuarioService.restablecerContrasena(id, contrasena);
            ra.addFlashAttribute("ok", "Contrasena restablecida. El usuario debera cambiarla al ingresar.");
        }
        return "redirect:/admin/usuarios/" + id;
    }

    private void cambiarEstado(Long id, boolean activo, RedirectAttributes ra) {
        try {
            usuarioService.cambiarActivo(id, activo);
            ra.addFlashAttribute("ok", activo ? "Cuenta activada." : "Cuenta desactivada.");
        } catch (OperacionInvalida e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
    }
}
