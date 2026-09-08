package co.edu.uts.portal.identidad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Rol administrativo. La tabla se siembra por Flyway (V2) con las dos filas del
 * enum {@link NombreRol}; la aplicacion no crea filas nuevas en tiempo de ejecucion.
 */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true, length = 40)
    private NombreRol nombre;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    protected Rol() {
    }

    public Rol(NombreRol nombre) {
        this.nombre = nombre;
        this.descripcion = nombre.getDescripcion();
    }

    public Long getId() {
        return id;
    }

    public NombreRol getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String authority() {
        return nombre.authority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rol otro)) {
            return false;
        }
        return nombre == otro.nombre;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nombre);
    }
}
