package co.edu.uts.portal.identidad.web;

import co.edu.uts.portal.identidad.service.LimitadorIntentosLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * RS-10.4: corta el intento de autenticacion ANTES de que Spring Security lo procese,
 * si el correo ya esta bloqueado por demasiados intentos fallidos recientes (ver
 * {@link LimitadorIntentosLogin}). Sin este filtro, un correo bloqueado seguiria
 * intentando validar la contrasena contra la base de datos en cada solicitud.
 */
public class LimitadorIntentosLoginFilter extends OncePerRequestFilter {

    private final LimitadorIntentosLogin limitador;

    public LimitadorIntentosLoginFilter(LimitadorIntentosLogin limitador) {
        this.limitador = limitador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        boolean esIntentoDeLogin = "POST".equalsIgnoreCase(request.getMethod())
                && "/login".equals(request.getServletPath());

        if (esIntentoDeLogin && limitador.estaBloqueado(request.getParameter("correo"))) {
            response.sendRedirect(request.getContextPath() + "/login?bloqueado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
