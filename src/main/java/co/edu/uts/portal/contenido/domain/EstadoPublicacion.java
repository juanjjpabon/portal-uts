package co.edu.uts.portal.contenido.domain;

/**
 * Estado de publicacion de un recurso (HU-01: el portal solo muestra PUBLICADO).
 */
public enum EstadoPublicacion {

    BORRADOR("Borrador"),
    PUBLICADO("Publicado"),
    ARCHIVADO("Archivado");

    private final String etiqueta;

    EstadoPublicacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
