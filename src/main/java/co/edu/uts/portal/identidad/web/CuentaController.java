package co.edu.uts.portal.identidad.web;

import co.edu.uts.portal.identidad.service.CuentaService;
import co.edu.uts.portal.identidad.service.OperacionInvalida;
import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import co.edu.uts.portal.identidad.web.dto.CambiarContrasenaForm;

/**
 * HU-21: autoservicio de contrasena. Sirve para el cambio voluntario y para el
 * obligatorio del primer ingreso (al que llega redirigido por el interceptor).
 */
@Controller
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/cuenta/contrasena")
    public String formulario(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        model.addAttribute("form", new CambiarContrasenaForm());
        model.addAttribute("obligatorio", principal != null && principal.debeCambiarClave());
        return "cuenta/contrasena";
    }

    @PostMapping("/cuenta/contrasena")
    public String cambiar(@AuthenticationPrincipal UsuarioAutenticado principal,
                          @Valid @ModelAttribute("form") CambiarContrasenaForm form, BindingResult errores,
                          Model model, HttpServletRequest request, HttpServletResponse response) {
        boolean obligatorio = principal.debeCambiarClave();
        model.addAttribute("obligatorio", obligatorio);
        if (!errores.hasErrors()) {
            try {
                cuentaService.cambiarContrasena(principal.getId(), form, !obligatorio);
            } catch (OperacionInvalida e) {
                model.addAttribute("error", e.getMessage());
                return "cuenta/contrasena";
            }
            // La contrasena cambio: se cierra la sesion y se pide ingresar de nuevo.
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?clave";
        }
        return "cuenta/contrasena";
    }
}
