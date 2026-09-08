package co.edu.uts.portal.identidad.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Al iniciar sesion correctamente (HU-16) registra el instante de acceso del usuario
 * y luego delega en el comportamiento estandar (redirige a la URL solicitada o a
 * defaultSuccessUrl).
 */
@Component
public class RegistroAccesoHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RegistroAccesoService registroAccesoService;

    public RegistroAccesoHandler(RegistroAccesoService registroAccesoService) {
        this.registroAccesoService = registroAccesoService;
        setDefaultTargetUrl("/admin");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof UsuarioAutenticado principal) {
            registroAccesoService.registrarAcceso(principal.getId(), Instant.now());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
