package co.edu.uts.portal.contenido.web.dto;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.domain.validacion.RecursoCoherente;
import co.edu.uts.portal.contenido.domain.validacion.RecursoValidable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO del formulario del panel (HU-18/HU-19). Se enlaza a la vista en lugar de la
 * entidad para no exponer los campos de auditoria y para dar mensajes claros.
 * Los campos obligatorios por tipo los valida {@link RecursoCoherente}.
 */
@RecursoCoherente
public class RecursoForm implements RecursoValidable {

    private Long id;

    @NotNull
    private TipoRecurso tipo;

    @NotBlank
    @Size(max = 160)
    private String titulo;

    // CONTENIDO
    @Size(max = 500)
    private String resumen;

    private String cuerpo;

    @Size(max = 300)
    private String fuente;

    // RUTA / CONTACTO
    @Size(max = 160)
    private String dependencia;

    @Size(max = 200)
    private String horario;

    @Size(max = 200)
    private String canal;

    @Size(max = 300)
    private String urlCanal;

    private boolean urgente;

    // imagen y portada (reunion con la directora, 22/9/2026)
    /** Archivo nuevo elegido en el formulario (opcional). Lo procesa ProcesadorImagen. */
    private MultipartFile imagenArchivo;

    /** Solo para mostrar la imagen actual en el formulario; el servicio usa la de la entidad. */
    private UUID imagenIdActual;

    private boolean quitarImagen;

    @Size(max = 250)
    private String imagenAlt;

    private boolean destacado;

    // comunes
    @NotNull
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    private int orden = 0;

    private Set<Long> categoriaIds = new LinkedHashSet<>();

    public static RecursoForm nuevo(TipoRecurso tipo) {
        RecursoForm f = new RecursoForm();
        f.tipo = tipo;
        return f;
    }

    public static RecursoForm de(Recurso r) {
        RecursoForm f = new RecursoForm();
        f.id = r.getId();
        f.tipo = r.getTipo();
        f.titulo = r.getTitulo();
        f.resumen = r.getResumen();
        f.cuerpo = r.getCuerpo();
        f.fuente = r.getFuente();
        f.dependencia = r.getDependencia();
        f.horario = r.getHorario();
        f.canal = r.getCanal();
        f.urlCanal = co.edu.uts.portal.contenido.domain.CanalDirecto.paraEditar(r.getUrlCanal());
        f.urgente = r.isUrgente();
        f.imagenIdActual = r.getImagenId();
        f.imagenAlt = r.getImagenAlt();
        f.destacado = r.isDestacado();
        f.estado = r.getEstado();
        f.orden = r.getOrden();
        r.getCategorias().forEach(c -> f.categoriaIds.add(c.getId()));
        return f;
    }

    @Override
    public boolean tieneCategorias() {
        return categoriaIds != null && !categoriaIds.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public TipoRecurso getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    @Override
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

    @Override
    public String getDependencia() {
        return dependencia;
    }

    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }

    @Override
    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    @Override
    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    @Override
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

    public MultipartFile getImagenArchivo() {
        return imagenArchivo;
    }

    public void setImagenArchivo(MultipartFile imagenArchivo) {
        this.imagenArchivo = imagenArchivo;
    }

    /** true si en este envio se eligio un archivo de imagen. */
    public boolean tieneImagenNueva() {
        return imagenArchivo != null && !imagenArchivo.isEmpty();
    }

    public UUID getImagenIdActual() {
        return imagenIdActual;
    }

    public void setImagenIdActual(UUID imagenIdActual) {
        this.imagenIdActual = imagenIdActual;
    }

    public boolean isQuitarImagen() {
        return quitarImagen;
    }

    public void setQuitarImagen(boolean quitarImagen) {
        this.quitarImagen = quitarImagen;
    }

    public String getImagenAlt() {
        return imagenAlt;
    }

    public void setImagenAlt(String imagenAlt) {
        this.imagenAlt = imagenAlt;
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

    public Set<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(Set<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }
}
