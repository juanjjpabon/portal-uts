package co.edu.uts.portal.cuestionario.domain;

/**
 * Estado de una version de cuestionario (HU-20).
 * BORRADOR es editable; PUBLICADA y ARCHIVADA son inmutables.
 */
public enum EstadoVersion {

    BORRADOR("Borrador"),
    PUBLICADA("Publicada"),
    ARCHIVADA("Archivada");

    private final String etiqueta;

    EstadoVersion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean esEditable() {
        return this == BORRADOR;
    }
}
