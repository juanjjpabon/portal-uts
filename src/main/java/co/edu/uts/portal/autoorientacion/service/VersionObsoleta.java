package co.edu.uts.portal.autoorientacion.service;

/**
 * El formulario se envio contra una version que ya no es la publicada (un
 * administrador publico una nueva mientras se respondia). El controlador pide
 * empezar de nuevo.
 */
public class VersionObsoleta extends RuntimeException {

    public VersionObsoleta(String mensaje) {
        super(mensaje);
    }
}
