package co.edu.uts.portal.cuestionario.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Version de un cuestionario (HU-20). En BORRADOR se edita libremente; al publicarse
 * queda inmutable y "un cambio genera una nueva version" (se copia a un BORRADOR nuevo).
 */
@Entity
@Table(name = "cuestionario_version",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cuestionario_id", "numero"}))
public class CuestionarioVersion extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cuestionario_id")
    private Cuestionario cuestionario;

    @Column(name = "numero", nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVersion estado = EstadoVersion.BORRADOR;

    @Column(name = "publicada_en")
    private Instant publicadaEn;

    @Column(name = "notas_version", length = 300)
    private String notasVersion;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden asc, id asc")
    private List<Pregunta> preguntas = new ArrayList<>();

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden asc, id asc")
    private List<NivelResultado> niveles = new ArrayList<>();

    protected CuestionarioVersion() {
    }

    CuestionarioVersion(Cuestionario cuestionario, int numero) {
        this.cuestionario = cuestionario;
        this.numero = numero;
    }

    public Pregunta agregarPregunta(String enunciado) {
        Pregunta p = new Pregunta(this, enunciado, siguienteOrden(preguntas.size()));
        preguntas.add(p);
        return p;
    }

    public NivelResultado agregarNivel(String nombre) {
        NivelResultado n = new NivelResultado(this, nombre, siguienteOrden(niveles.size()));
        niveles.add(n);
        return n;
    }

    private int siguienteOrden(int size) {
        return (size + 1) * 10;
    }

    public void publicar(Instant momento) {
        this.estado = EstadoVersion.PUBLICADA;
        this.publicadaEn = momento;
    }

    public void archivar() {
        this.estado = EstadoVersion.ARCHIVADA;
    }

    public boolean esEditable() {
        return estado.esEditable();
    }

    public Long getId() {
        return id;
    }

    public Cuestionario getCuestionario() {
        return cuestionario;
    }

    public int getNumero() {
        return numero;
    }

    public EstadoVersion getEstado() {
        return estado;
    }

    public Instant getPublicadaEn() {
        return publicadaEn;
    }

    public String getNotasVersion() {
        return notasVersion;
    }

    public void setNotasVersion(String notasVersion) {
        this.notasVersion = notasVersion;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public List<NivelResultado> getNiveles() {
        return niveles;
    }
}
