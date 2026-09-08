package co.edu.uts.portal.identidad.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticacionController {

    /** HU-16: pagina de inicio de sesion. */
    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
        return autenticado ? "redirect:/admin" : "auth/login";
    }
}
