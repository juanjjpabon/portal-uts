package co.edu.uts.portal.config;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.service.LimitadorIntentosLogin;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import co.edu.uts.portal.identidad.web.LimitadorIntentosLoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * HU-16 (login/logout) y HU-17 (permisos por rol, separacion estricta).
 *
 * Defensa en dos capas:
 *  - Por URL: authorizeHttpRequests (abajo).
 *  - Por metodo: @EnableMethodSecurity + @PreAuthorize en los @Service.
 *
 * CSRF queda ACTIVO: Thymeleaf inyecta el token en todos los formularios.
 *
 * RS-10.1 (CORS explicito), RS-10.3 (Content-Security-Policy) y RS-10.4 (limite de
 * intentos de login) agregados el 28/9/2026 -- ver comentario 64 de la directora y
 * claude/plan_rs_4.3_seguridad.md para el detalle y la evidencia de cada control.
 * RS-03.3 (expiracion de sesion por inactividad) se configuro en application.yml
 * (server.servlet.session.timeout), no aqui.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String FUNCIONAL = NombreRol.ADMIN_FUNCIONAL.name();
    private static final String TECNICO = NombreRol.ADMIN_TECNICO.name();

    // RS-10.3: el portal solo carga recursos propios y Bootstrap por CDN (ver
    // fragments/comunes.html: cdn.jsdelivr.net, unico dominio externo del proyecto).
    // 'unsafe-inline' en script-src/style-src queda porque varias plantillas usan
    // manejadores onclick= y atributos style= en linea (verificado: 6 plantillas con
    // onclick=, 21 con style=, 2 con <script> propio). Retirarlo exigiria refactorizar
    // esas plantillas para usar nonces o mover ese codigo a archivos externos -- queda
    // fuera de esta ronda, documentado como limitacion conocida (no cumple el ASVS L2
    // completo de V3.4.3-V3.4.6, que pide evitar unsafe-inline).
    private static final String CSP_DIRECTIVAS = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'",
            "style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'",
            "img-src 'self' data:",
            "font-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'"
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // RS-10.1: politica de CORS explicita. El portal no expone ninguna API publica
    // consumida desde otro origen (todo el frontend lo sirve el mismo servidor), asi
    // que no se declara ningun origen permitido. Antes esto dependia implicitamente
    // de que Spring Security nunca habilitara CORS por defecto (sin ningun bean en el
    // codigo, ver plan_rs_4.3_seguridad.md); ahora la decision queda explicita y
    // auditable, sin cambiar el comportamiento real (sigue sin permitir ningun origen
    // cruzado).
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of());
        configuracion.setAllowedMethods(List.of());
        configuracion.setAllowedHeaders(List.of());

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RegistroAccesoHandler registroAccesoHandler,
                                           LimitadorIntentosLogin limitadorIntentosLogin) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP_DIRECTIVAS))
            )
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos y contenido publico del portal (M01-M04)
                .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/error", "/error/**").permitAll()
                .requestMatchers("/contenidos/**", "/rutas/**", "/autoorientacion", "/autoorientacion/**",
                        "/urgencia", "/buscar", "/valoraciones/**", "/imagenes/**").permitAll()

                // Panel administrativo - HU-17 separacion estricta
                .requestMatchers("/admin/usuarios/**", "/admin/bitacora/**", "/admin/sistema/**").hasRole(TECNICO)
                .requestMatchers("/admin/recursos/**", "/admin/categorias/**", "/admin/cuestionarios/**",
                        "/admin/parametros/**", "/admin/analitica/**").hasRole(FUNCIONAL)
                .requestMatchers("/admin", "/admin/").hasAnyRole(FUNCIONAL, TECNICO)

                .anyRequest().authenticated()
            )
            // RS-10.4: se registra ANTES del filtro de autenticacion de Spring Security,
            // para que un correo ya bloqueado ni siquiera llegue a validar contrasena.
            .addFilterBefore(new LimitadorIntentosLoginFilter(limitadorIntentosLogin),
                    UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("clave")
                .successHandler(registroAccesoHandler)
                // RS-10.4: cada fallo cuenta para el bloqueo temporal (LimitadorIntentosLogin);
                // el mensaje que ve el usuario en un fallo normal no cambia (/login?error).
                .failureHandler((request, response, exception) -> {
                    limitadorIntentosLogin.registrarFallo(request.getParameter("correo"));
                    response.sendRedirect(request.getContextPath() + "/login?error");
                })
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
