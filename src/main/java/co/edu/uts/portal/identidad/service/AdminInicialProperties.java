package co.edu.uts.portal.identidad.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Datos del administrador tecnico inicial (portal.seguridad.admin-inicial.*).
 * La contrasena llega de la variable de entorno PORTAL_ADMIN_INICIAL_PASSWORD;
 * nunca se guarda en el codigo ni en el repositorio.
 */
@ConfigurationProperties(prefix = "portal.seguridad.admin-inicial")
public record AdminInicialProperties(String correo, String nombre, String password) {
}
