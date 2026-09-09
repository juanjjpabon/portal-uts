package co.edu.uts.portal.cuestionario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "opcion")
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pregunta_id")
    private Pregunta pregunta;

    @Column(name = "texto", nullable = false, length = 200)
    private String texto;

    /** Puntaje que aporta esta opcion al total (HU-10). */
    @Column(name = "valor", nullable = false)
    private int valor;

    @Column(name = "orden", nullable = false)
    private int orden;

    protected Opcion() {
    }

    Opcion(Pregunta pregunta, String texto, int valor, int orden) {
        this.pregunta = pregunta;
        this.texto = texto;
        this.valor = valor;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Pregunta getPregunta() {
        return pregunta;
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
