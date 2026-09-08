package co.edu.uts.portal.config;

import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Habilita @CreatedBy / @LastModifiedBy (HU-18, HU-22): el "responsable" registrado
 * es el correo del administrador autenticado, o "sistema" para tareas de arranque.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("sistema");
            }
            if (auth.getPrincipal() instanceof UsuarioAutenticado principal) {
                return Optional.of(principal.getCorreo());
            }
            return Optional.of(auth.getName());
        };
    }
}
