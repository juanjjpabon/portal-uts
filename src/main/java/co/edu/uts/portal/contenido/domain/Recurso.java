package co.edu.uts.portal.contenido.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Recurso generico del portal (F-DC-125): un mismo modelo y un mismo CRUD para
 * CONTENIDO (HU-04/HU-18), RUTA (HU-05/HU-19) y CONTACTO. Los campos que no
 * aplican a un {@link TipoRecurso} quedan nulos; {@link RecursoCoherente} valida
 * que esten presentes los que si aplican.
 *
 * La "fecha de actualizacion / ultima revision" (HU-04/HU-05) y el responsable
 * (HU-18) provienen de {@link BaseAuditable}.
 */
@Entity
@Table(name = "recurso")
public class Recurso extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false, length = 20)
    private TipoRecurso tipo;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    /** Solo CONTENIDO: identificador para la URL publica (HU-01). */
    @Column(name = "slug", unique = true, length = 180)
    private String slug;

    // --- CONTENIDO (HU-04) ---
    @Column(name = "resumen", length = 500)
    private String resumen;

    @Column(name = "cuerpo", columnDefinition = "text")
    private String cuerpo;

    @Column(name = "fuente", length = 300)
    private String fuente;

    // --- RUTA / CONTACTO (HU-05) ---
    @Column(name = "dependencia", length = 160)
    private String dependencia;

    @Column(name = "horario", length = 200)
    private String horario;

    @Column(name = "canal", length = 200)
    private String canal;

    /** Habilita el boton de canal directo (HU-07). */
    @Column(name = "url_canal", length = 300)
    private String urlCanal;

    /** Marca la ruta/contacto como informacion para situaciones urgentes (HU-06/HU-19). */
    @Column(name = "urgente", nullable = false)
    private boolean urgente = false;

    // --- imagen y portada (reunion con la directora, 22/9/2026) ---
    /** Imagen principal (folleto, banner, foto). Solo el id: los bytes se leen al servir /imagenes/{id}. */
    @Column(name = "imagen_id")
    private UUID imagenId;

    /** Texto alternativo de la imagen para lectores de pantalla (accesibilidad). */
    @Column(name = "imagen_alt", length = 250)
    private String imagenAlt;

    /** Aparece en el carrusel de la portada (solo si tiene imagen y esta publicado). */
    @Column(name = "destacado", nullable = false)
    private boolean destacado = false;

    // --- comunes ---
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    @Column(name = "orden", nullable = false)
    private int orden = 0;

    @Column(name = "publicado_en")
    private Instant publicadoEn;

    // --- estadisticas agregadas (HU-23/HU-25): solo contadores, nunca un registro
    // por visita o por voto. Se incrementan con un UPDATE atomico del repositorio;
    // por eso no llevan setter.
    @Column(name = "vistas", nullable = false)
    private int vistas = 0;

    @Column(name = "valoraciones_util", nullable = false)
    private int valoracionesUtil = 0;

    @Column(name = "valoraciones_no_util", nullable = false)
    private int valoracionesNoUtil = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recurso_categoria",
            joinColumns = @JoinColumn(name = "recurso_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new LinkedHashSet<>();

    protected Recurso() {
    }

    public Recurso(TipoRecurso tipo) {
        this.tipo = tipo;
    }

    /** Pasa a PUBLICADO y fija la fecha de publicacion si aun no la tiene (HU-01). */
    public void publicar(Instant momento) {
        this.estado = EstadoPublicacion.PUBLICADO;
        if (this.publicadoEn == null) {
            this.publicadoEn = momento;
        }
    }

    public void archivar() {
        this.estado = EstadoPublicacion.ARCHIVADO;
    }

    public void reemplazarCategorias(Set<Categoria> nuevas) {
        this.categorias.clear();
        this.categorias.addAll(nuevas);
    }

    public Long getId() {
        return id;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDependencia() {
        return dependencia;
    }

    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getUrlCanal() {
        return urlCanal;
    }

    public void setUrlCanal(String urlCanal) {
        this.urlCanal = urlCanal;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    /**
     * Contacto directo listo para mostrar (correo o telefono a la vista con boton de
     * copiar, enlace web, WhatsApp). Null si no tiene o no es valido. No se persiste.
     */
    public CanalDirecto getCanalDirecto() {
        return CanalDirecto.desde(urlCanal).orElse(null);
    }

    public UUID getImagenId() {
        return imagenId;
    }

    public void setImagenId(UUID imagenId) {
        this.imagenId = imagenId;
    }

    public boolean isTieneImagen() {
        return imagenId != null;
    }

    public String getImagenAlt() {
        return imagenAlt;
    }

    public void setImagenAlt(String imagenAlt) {
        this.imagenAlt = imagenAlt;
    }

    /** Texto alternativo efectivo: el que escribio el administrador, o el titulo del recurso. */
    public String getTextoAlternativo() {
        return imagenAlt != null && !imagenAlt.isBlank() ? imagenAlt : titulo;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
    }

    public EstadoPublicacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPublicacion estado) {
        this.estado = estado;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public Instant getPublicadoEn() {
        return publicadoEn;
    }

    public Set<Categoria> getCategorias() {
        return categorias;
    }

    public int getVistas() {
        return vistas;
    }

    public int getValoracionesUtil() {
        return valoracionesUtil;
    }

    public int getValoracionesNoUtil() {
        return valoracionesNoUtil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recurso otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}
