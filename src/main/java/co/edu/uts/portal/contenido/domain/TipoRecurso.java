package co.edu.uts.portal.contenido.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Tipos del recurso generico (F-DC-125). Un unico CRUD atiende los tres; la
 * "seccion" es el segmento de URL del panel (/admin/recursos/{seccion}).
 */
public enum TipoRecurso {

    CONTENIDO("contenidos", "Contenido", "Contenidos"),
    RUTA("rutas", "Ruta institucional", "Rutas institucionales"),
    CONTACTO("contactos", "Contacto", "Contactos");

    private final String seccion;
    private final String etiquetaSingular;
    private final String etiquetaPlural;

    TipoRecurso(String seccion, String etiquetaSingular, String etiquetaPlural) {
        this.seccion = seccion;
        this.etiquetaSingular = etiquetaSingular;
        this.etiquetaPlural = etiquetaPlural;
    }

    public String getSeccion() {
        return seccion;
    }

    public String getEtiquetaSingular() {
        return etiquetaSingular;
    }

    public String getEtiquetaPlural() {
        return etiquetaPlural;
    }

    /** true si el tipo maneja resumen/cuerpo/fuente (HU-04). */
    public boolean esContenido() {
        return this == CONTENIDO;
    }

    /** true si el tipo maneja dependencia/horario/canal (HU-05). */
    public boolean esRutaOContacto() {
        return this == RUTA || this == CONTACTO;
    }

    public static Optional<TipoRecurso> porSeccion(String seccion) {
        return Arrays.stream(values())
                .filter(t -> t.seccion.equalsIgnoreCase(seccion))
                .findFirst();
    }
}
