package co.edu.uts.portal.cuestionario.service;

/**
 * Se intento modificar una version PUBLICADA o ARCHIVADA (HU-20: una version en uso
 * no se altera retroactivamente). El controlador la traduce a un mensaje flash.
 */
public class VersionNoEditable extends RuntimeException {

    public VersionNoEditable(String mensaje) {
        super(mensaje);
    }
}
