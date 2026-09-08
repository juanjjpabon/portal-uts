package co.edu.uts.portal.contenido.web.dto;

import co.edu.uts.portal.contenido.domain.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    private Long id;

    @NotBlank
    @Size(max = 80)
    private String nombre;

    @Size(max = 300)
    private String descripcion;

    private boolean activa = true;

    private int orden = 0;

    public static CategoriaForm nueva() {
        return new CategoriaForm();
    }

    public static CategoriaForm de(Categoria c) {
        CategoriaForm f = new CategoriaForm();
        f.id = c.getId();
        f.nombre = c.getNombre();
        f.descripcion = c.getDescripcion();
        f.activa = c.isActiva();
        f.orden = c.getOrden();
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
