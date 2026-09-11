package co.edu.uts.portal.bitacora.domain;

/**
 * Tipos de accion administrativa registrable (HU-22). Se amplia a medida que otros
 * modulos se conectan a la bitacora.
 */
public enum AccionBitacora {

    USUARIO_CREADO("Creo un usuario"),
    USUARIO_ACTUALIZADO("Actualizo un usuario"),
    USUARIO_ACTIVADO("Activo un usuario"),
    USUARIO_DESACTIVADO("Desactivo un usuario"),
    ROL_ASIGNADO("Asigno un rol"),
    ROL_REVOCADO("Revoco un rol"),
    CONTRASENA_RESTABLECIDA("Restablecio una contrasena"),
    CONTRASENA_CAMBIADA("Cambio su propia contrasena"),

    RECURSO_CREADO("Creo un recurso"),
    RECURSO_ACTUALIZADO("Actualizo un recurso"),
    RECURSO_PUBLICADO("Publico un recurso"),
    RECURSO_ARCHIVADO("Archivo un recurso"),
    RECURSO_ELIMINADO("Elimino un recurso"),

    CATEGORIA_CREADA("Creo una categoria"),
    CATEGORIA_ACTUALIZADA("Actualizo una categoria"),
    CATEGORIA_ELIMINADA("Elimino una categoria"),

    CUESTIONARIO_CREADO("Creo un cuestionario"),
    CUESTIONARIO_ACTUALIZADO("Actualizo los datos de un cuestionario"),
    VERSION_CREADA("Creo una nueva version del cuestionario"),
    VERSION_PUBLICADA("Publico una version del cuestionario"),
    VERSION_ARCHIVADA("Archivo una version del cuestionario"),

    PARAMETRO_ACTUALIZADO("Actualizo un parametro operativo");

    private final String etiqueta;

    AccionBitacora(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
