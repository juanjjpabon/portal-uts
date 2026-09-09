package co.edu.uts.portal.cuestionario.web.dto;

import co.edu.uts.portal.cuestionario.domain.Pregunta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PreguntaForm {

    private Long id;

    @NotBlank
    @Size(max = 300)
    private String enunciado;

    private int orden = 0;

    private boolean obligatoria = true;

    public static PreguntaForm de(Pregunta p) {
        PreguntaForm f = new PreguntaForm();
        f.id = p.getId();
        f.enunciado = p.getEnunciado();
        f.orden = p.getOrden();
        f.obligatoria = p.isObligatoria();
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }
}
