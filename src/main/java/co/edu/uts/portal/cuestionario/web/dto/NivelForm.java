package co.edu.uts.portal.cuestionario.web.dto;

import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class NivelForm {

    private Long id;

    @NotBlank
    @Size(max = 80)
    private String nombre;

    private int orden = 0;

    @PositiveOrZero
    private int puntajeMin = 0;

    @PositiveOrZero
    private int puntajeMax = 0;

    @NotBlank
    @Size(max = 1000)
    private String explicacion;

    @Size(max = 20)
    private String color;

    public static NivelForm de(NivelResultado n) {
        NivelForm f = new NivelForm();
        f.id = n.getId();
        f.nombre = n.getNombre();
        f.orden = n.getOrden();
        f.puntajeMin = n.getPuntajeMin();
        f.puntajeMax = n.getPuntajeMax();
        f.explicacion = n.getExplicacion();
        f.color = n.getColor();
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

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getPuntajeMin() {
        return puntajeMin;
    }

    public void setPuntajeMin(int puntajeMin) {
        this.puntajeMin = puntajeMin;
    }

    public int getPuntajeMax() {
        return puntajeMax;
    }

    public void setPuntajeMax(int puntajeMax) {
        this.puntajeMax = puntajeMax;
    }

    public String getExplicacion() {
        return explicacion;
    }

    public void setExplicacion(String explicacion) {
        this.explicacion = explicacion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
