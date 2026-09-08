package co.edu.uts.portal.contenido.web.dto;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.domain.validacion.RecursoCoherente;
import co.edu.uts.portal.contenido.domain.validacion.RecursoValidable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

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
        f.urlCanal = r.getUrlCanal();
        f.urgente = r.isUrgente();
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
