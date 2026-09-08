package co.edu.uts.portal.common.web;

import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dashboard del panel administrativo. El menu se pinta segun el rol (HU-17); la
 * autorizacion real la aplica SecurityConfig y los @PreAuthorize de cada modulo.
 */
@Controller
public class PanelController {

    @GetMapping("/admin")
    public String dashboard(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        model.addAttribute("correo", principal.getCorreo());
        return "admin/dashboard";
    }
}
