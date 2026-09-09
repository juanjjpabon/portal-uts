package co.edu.uts.portal.cuestionario.domain;

import co.edu.uts.portal.contenido.domain.Recurso;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Nivel orientativo de resultado y su regla (HU-10): la suma de puntajes cae en la
 * banda [puntajeMin, puntajeMax]. Lleva la explicacion para el resumen (HU-11),
 * sus recomendaciones (HU-12) y las rutas aplicables (HU-12, N:M con Recurso RUTA).
 */
@Entity
@Table(name = "nivel_resultado")
public class NivelResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "version_id")
    private CuestionarioVersion version;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "puntaje_min", nullable = false)
    private int puntajeMin;

    @Column(name = "puntaje_max", nullable = false)
    private int puntajeMax;

    @Column(name = "explicacion", nullable = false, length = 1000)
    private String explicacion;

    @Column(name = "color", length = 20)
    private String color;

    @OneToMany(mappedBy = "nivel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden asc, id asc")
    private List<Recomendacion> recomendaciones = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "nivel_ruta",
            joinColumns = @JoinColumn(name = "nivel_id"),
            inverseJoinColumns = @JoinColumn(name = "recurso_id")
    )
    private Set<Recurso> rutas = new LinkedHashSet<>();

    protected NivelResultado() {
    }

    NivelResultado(CuestionarioVersion version, String nombre, int orden) {
        this.version = version;
        this.nombre = nombre;
        this.orden = orden;
        this.explicacion = "";
    }

    public Recomendacion agregarRecomendacion(String texto) {
        Recomendacion r = new Recomendacion(this, texto, (recomendaciones.size() + 1) * 10);
        recomendaciones.add(r);
        return r;
    }

    public void reemplazarRutas(Set<Recurso> nuevas) {
        this.rutas.clear();
        this.rutas.addAll(nuevas);
    }

    public boolean contiene(int puntaje) {
        return puntaje >= puntajeMin && puntaje <= puntajeMax;
    }

    public Long getId() {
        return id;
    }

    public CuestionarioVersion getVersion() {
        return version;
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

    public List<Recomendacion> getRecomendaciones() {
        return recomendaciones;
    }

    public Set<Recurso> getRutas() {
        return rutas;
    }
}
