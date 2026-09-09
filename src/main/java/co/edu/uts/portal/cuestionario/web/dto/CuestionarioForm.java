package co.edu.uts.portal.cuestionario.web.dto;

import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CuestionarioForm {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    private boolean activo = true;

    public static CuestionarioForm nuevo() {
        return new CuestionarioForm();
    }

    public static CuestionarioForm de(Cuestionario c) {
        CuestionarioForm f = new CuestionarioForm();
        f.id = c.getId();
        f.nombre = c.getNombre();
        f.descripcion = c.getDescripcion();
        f.activo = c.isActivo();
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
