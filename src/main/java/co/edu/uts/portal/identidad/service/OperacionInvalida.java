package co.edu.uts.portal.identidad.service;

/**
 * Regla de negocio de gestion de usuarios que impide completar una operacion
 * (correo duplicado, dejar el sistema sin administrador tecnico activo,
 * autodesactivarse, etc.). El controlador la traduce a un mensaje flash.
 */
public class OperacionInvalida extends RuntimeException {

    public OperacionInvalida(String mensaje) {
        super(mensaje);
    }
}
