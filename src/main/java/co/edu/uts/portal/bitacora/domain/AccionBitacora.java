package co.edu.uts.portal.bitacora.domain;

/**
 * Tipos de accion administrativa registrable (HU-22). Se amplia a medida que otros
 * modulos se conectan a la bitacora.
 */
public enum AccionBitacora {

    USUARIO_CREADO("Creó un usuario"),
    USUARIO_ACTUALIZADO("Actualizó un usuario"),
    USUARIO_ACTIVADO("Activó un usuario"),
    USUARIO_DESACTIVADO("Desactivó un usuario"),
    ROL_ASIGNADO("Asignó un rol"),
    ROL_REVOCADO("Revocó un rol"),
    CONTRASENA_RESTABLECIDA("Restableció una contraseña"),
    CONTRASENA_CAMBIADA("Cambió su propia contraseña"),

    RECURSO_CREADO("Creó un recurso"),
    RECURSO_ACTUALIZADO("Actualizó un recurso"),
    RECURSO_PUBLICADO("Publicó un recurso"),
    RECURSO_ARCHIVADO("Archivó un recurso"),
    RECURSO_ELIMINADO("Eliminó un recurso"),

    CATEGORIA_CREADA("Creó una categoría"),
    CATEGORIA_ACTUALIZADA("Actualizó una categoría"),
    CATEGORIA_ELIMINADA("Eliminó una categoría"),

    CUESTIONARIO_CREADO("Creó un cuestionario"),
    CUESTIONARIO_ACTUALIZADO("Actualizó los datos de un cuestionario"),
    VERSION_CREADA("Creó una nueva versión del cuestionario"),
    VERSION_PUBLICADA("Publicó una versión del cuestionario"),
    VERSION_ARCHIVADA("Archivó una versión del cuestionario"),

    PARAMETRO_ACTUALIZADO("Actualizó un parámetro operativo");

    private final String etiqueta;

    AccionBitacora(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
