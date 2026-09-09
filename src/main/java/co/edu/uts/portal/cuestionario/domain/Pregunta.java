package co.edu.uts.portal.cuestionario.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Pregunta de opcion unica (HU-09/HU-20). El aporte al puntaje es el valor de la
 * unica opcion elegida.
 */
@Entity
@Table(name = "pregunta")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "version_id")
    private CuestionarioVersion version;

    @Column(name = "enunciado", nullable = false, length = 300)
    private String enunciado;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "obligatoria", nullable = false)
    private boolean obligatoria = true;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden asc, id asc")
    private List<Opcion> opciones = new ArrayList<>();

    protected Pregunta() {
    }

    Pregunta(CuestionarioVersion version, String enunciado, int orden) {
        this.version = version;
        this.enunciado = enunciado;
        this.orden = orden;
    }

    public Opcion agregarOpcion(String texto, int valor) {
        Opcion o = new Opcion(this, texto, valor, (opciones.size() + 1) * 10);
        opciones.add(o);
        return o;
    }

    public int valorMaximo() {
        return opciones.stream().mapToInt(Opcion::getValor).max().orElse(0);
    }

    public int valorMinimo() {
        return opciones.stream().mapToInt(Opcion::getValor).min().orElse(0);
    }

    public Long getId() {
        return id;
    }

    public CuestionarioVersion getVersion() {
        return version;
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

    public List<Opcion> getOpciones() {
        return opciones;
    }
}
