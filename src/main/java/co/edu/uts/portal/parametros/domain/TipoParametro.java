package co.edu.uts.portal.parametros.domain;

/** Determina el control de edicion en el panel (HU-27). */
public enum TipoParametro {

    TEXTO_CORTO("Texto corto"),
    TEXTO_LARGO("Texto largo"),
    URL("URL");

    private final String etiqueta;

    TipoParametro(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
