package co.edu.uts.portal.identidad.domain;

/**
 * Catalogo fijo de roles administrativos (HU-17). El administrador tecnico asigna o
 * revoca estos roles a los usuarios (HU-21), pero no crea tipos de rol nuevos.
 *
 * La autoridad de Spring Security es "ROLE_" + name(), p. ej. ROLE_ADMIN_TECNICO.
 */
public enum NombreRol {

    ADMIN_FUNCIONAL("Administrador funcional",
            "Gestiona contenidos, categorías, rutas, contactos, cuestionarios, parámetros y analítica."),

    ADMIN_TECNICO("Administrador técnico",
            "Gestiona usuarios y roles, bitácora y configuración técnica del portal.");

    private final String etiqueta;
    private final String descripcion;

    NombreRol(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Authority usada en authorizeHttpRequests y @PreAuthorize. */
    public String authority() {
        return "ROLE_" + name();
    }
}
