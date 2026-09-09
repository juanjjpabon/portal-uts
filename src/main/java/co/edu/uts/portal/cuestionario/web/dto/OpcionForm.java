package co.edu.uts.portal.cuestionario.web.dto;

import co.edu.uts.portal.cuestionario.domain.Opcion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class OpcionForm {

    private Long id;

    @NotBlank
    @Size(max = 200)
    private String texto;

    @PositiveOrZero
    private int valor = 0;

    private int orden = 0;

    public static OpcionForm de(Opcion o) {
        OpcionForm f = new OpcionForm();
        f.id = o.getId();
        f.texto = o.getTexto();
        f.valor = o.getValor();
        f.orden = o.getOrden();
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
