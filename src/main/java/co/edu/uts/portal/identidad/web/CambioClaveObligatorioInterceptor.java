package co.edu.uts.portal.identidad.web;

import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * HU-21: mientras el usuario autenticado tenga la marca "debe cambiar clave", se le
 * redirige a /cuenta/contrasena. Se permiten esa pagina, el logout y los recursos
 * estaticos para no dejarlo atrapado.
 */
public class CambioClaveObligatorioInterceptor implements HandlerInterceptor {

    private static final Set<String> PERMITIDAS = Set.of(
            "/cuenta/contrasena", "/logout", "/login", "/error", "/favicon.ico");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado principal)
                || !principal.debeCambiarClave()) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (PERMITIDAS.contains(path) || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/img/") || path.startsWith("/webjars/")) {
            return true;
        }
        try {
            response.sendRedirect(request.getContextPath() + "/cuenta/contrasena");
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}
