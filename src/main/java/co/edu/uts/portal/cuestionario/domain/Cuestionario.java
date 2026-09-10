package co.edu.uts.portal.cuestionario.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Identidad estable de un instrumento de autoorientacion (HU-20). El contenido
 * (preguntas, niveles, recomendaciones) vive en cada {@link CuestionarioVersion};
 * este objeto solo agrupa las versiones y sus datos de presentacion.
 */
@Entity
@Table(name = "cuestionario")
public class Cuestionario extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    /** Identificador para la URL publica de autoorientacion (M03). Se fija al crear. */
    @Column(name = "slug", nullable = false, unique = true, length = 140)
    private String slug;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero asc")
    private List<CuestionarioVersion> versiones = new ArrayList<>();

    protected Cuestionario() {
    }

    public Cuestionario(String nombre, String slug) {
        this.nombre = nombre;
        this.slug = slug;
    }

    /** Conveniencia (tests / usos internos): deriva el slug del nombre. */
    public Cuestionario(String nombre) {
        this(nombre, co.edu.uts.portal.common.Slugs.de(nombre));
    }

    public CuestionarioVersion nuevaVersionVacia() {
        CuestionarioVersion v = new CuestionarioVersion(this, siguienteNumero());
        versiones.add(v);
        return v;
    }

    public int siguienteNumero() {
        return versiones.stream().mapToInt(CuestionarioVersion::getNumero).max().orElse(0) + 1;
    }

    public Optional<CuestionarioVersion> versionPublicada() {
        return versiones.stream()
                .filter(v -> v.getEstado() == EstadoVersion.PUBLICADA)
                .findFirst();
    }

    public Optional<CuestionarioVersion> ultimaVersion() {
        return versiones.stream().max(Comparator.comparingInt(CuestionarioVersion::getNumero));
    }

    public Optional<CuestionarioVersion> version(int numero) {
        return versiones.stream().filter(v -> v.getNumero() == numero).findFirst();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
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

    public List<CuestionarioVersion> getVersiones() {
        return versiones;
    }
}
