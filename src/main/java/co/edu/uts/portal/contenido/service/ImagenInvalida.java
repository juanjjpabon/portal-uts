package co.edu.uts.portal.contenido.service;

/**
 * La imagen subida no se puede aceptar. El mensaje esta pensado para mostrarse tal
 * cual al administrador (personal del CAE, no tecnico): dice que paso y que hacer.
 */
public class ImagenInvalida extends RuntimeException {

    public ImagenInvalida(String mensaje) {
        super(mensaje);
    }
}
