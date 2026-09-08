package co.edu.uts.portal.contenido.service;

/**
 * Regla de negocio que impide completar una operacion (p. ej. eliminar una
 * categoria en uso). El controlador la traduce a un mensaje flash.
 */
public class OperacionNoPermitida extends RuntimeException {

    public OperacionNoPermitida(String mensaje) {
        super(mensaje);
    }
}
