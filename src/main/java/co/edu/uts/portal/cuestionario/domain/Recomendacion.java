package co.edu.uts.portal.cuestionario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Recomendacion textual asociada a un nivel de resultado (HU-12). */
@Entity
@Table(name = "recomendacion")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nivel_id")
    private NivelResultado nivel;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "orden", nullable = false)
    private int orden;

    protected Recomendacion() {
    }

    Recomendacion(NivelResultado nivel, String texto, int orden) {
        this.nivel = nivel;
        this.texto = texto;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public NivelResultado getNivel() {
        return nivel;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
