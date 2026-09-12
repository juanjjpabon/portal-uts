package co.edu.uts.portal.config;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HU-16 (login/logout) y HU-17 (permisos por rol, separacion estricta).
 *
 * Defensa en dos capas:
 *  - Por URL: authorizeHttpRequests (abajo).
 *  - Por metodo: @EnableMethodSecurity + @PreAuthorize en los @Service.
 *
 * CSRF queda ACTIVO: Thymeleaf inyecta el token en todos los formularios.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String FUNCIONAL = NombreRol.ADMIN_FUNCIONAL.name();
    private static final String TECNICO = NombreRol.ADMIN_TECNICO.name();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RegistroAccesoHandler registroAccesoHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos y contenido publico del portal (M01-M04)
                .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/error", "/error/**").permitAll()
                .requestMatchers("/contenidos/**", "/rutas/**", "/autoorientacion", "/autoorientacion/**",
                        "/urgencia", "/buscar", "/valoraciones/**").permitAll()

                // Panel administrativo - HU-17 separacion estricta
                .requestMatchers("/admin/usuarios/**", "/admin/bitacora/**", "/admin/sistema/**").hasRole(TECNICO)
                .requestMatchers("/admin/recursos/**", "/admin/categorias/**", "/admin/cuestionarios/**",
                        "/admin/parametros/**", "/admin/analitica/**").hasRole(FUNCIONAL)
                .requestMatchers("/admin", "/admin/").hasAnyRole(FUNCIONAL, TECNICO)

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("clave")
                .successHandler(registroAccesoHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))
            .sessionManagement(session -> session
                .sessionFixation(sf -> sf.changeSessionId())
            );

        return http.build();
    }
}
