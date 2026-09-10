package co.edu.uts.portal.bitacora.domain;

/**
 * Tipos de accion administrativa registrable (HU-22). Se amplia a medida que otros
 * modulos se conectan a la bitacora; HU-21 usa las de usuarios y roles.
 */
public enum AccionBitacora {

    USUARIO_CREADO("Creo un usuario"),
    USUARIO_ACTUALIZADO("Actualizo un usuario"),
    USUARIO_ACTIVADO("Activo un usuario"),
    USUARIO_DESACTIVADO("Desactivo un usuario"),
    ROL_ASIGNADO("Asigno un rol"),
    ROL_REVOCADO("Revoco un rol"),
    CONTRASENA_RESTABLECIDA("Restablecio una contrasena"),
    CONTRASENA_CAMBIADA("Cambio su propia contrasena");

    private final String etiqueta;

    AccionBitacora(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
