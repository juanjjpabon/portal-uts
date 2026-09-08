package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.common.Slugs;
import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CRUD unico para CONTENIDO, RUTA y CONTACTO (F-DC-125, HU-18/HU-19).
 * Lecturas abiertas al panel; mutaciones restringidas a ADMIN_FUNCIONAL (HU-17).
 * El responsable y la fecha de cada cambio los pone el AuditorAware (HU-18).
 */
@Service
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final CategoriaRepository categoriaRepository;

    public RecursoService(RecursoRepository recursoRepository, CategoriaRepository categoriaRepository) {
        this.recursoRepository = recursoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Recurso> listar(TipoRecurso tipo, EstadoPublicacion estado, Long categoriaId, String texto) {
        String t = StringUtils.hasText(texto) ? texto.trim() : null;
        return recursoRepository.buscarParaPanel(tipo, estado, categoriaId, t);
    }

    @Transactional(readOnly = true)
    public Recurso obtener(Long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Recurso " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public Recurso obtenerDeTipo(Long id, TipoRecurso tipo) {
        Recurso r = obtener(id);
        if (r.getTipo() != tipo) {
            throw new RecursoNoEncontrado("El recurso " + id + " no es de tipo " + tipo);
        }
        return r;
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public Recurso crear(RecursoForm form) {
        Recurso r = new Recurso(form.getTipo());
        aplicar(r, form);
        return recursoRepository.save(r);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizar(Long id, RecursoForm form) {
        Recurso r = obtenerDeTipo(id, form.getTipo());
        aplicar(r, form);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void publicar(Long id, TipoRecurso tipo) {
        obtenerDeTipo(id, tipo).publicar(Instant.now());
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void archivar(Long id, TipoRecurso tipo) {
        obtenerDeTipo(id, tipo).archivar();
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void eliminar(Long id, TipoRecurso tipo) {
        recursoRepository.delete(obtenerDeTipo(id, tipo));
    }

    private void aplicar(Recurso r, RecursoForm form) {
        r.setTitulo(form.getTitulo().trim());
        r.setEstado(form.getEstado());
        r.setOrden(form.getOrden());

        if (r.getTipo().esContenido()) {
            r.setResumen(form.getResumen());
            r.setCuerpo(form.getCuerpo());
            r.setFuente(form.getFuente());
            r.setSlug(slugUnico(form.getTitulo(), r.getId()));
            limpiarCamposRuta(r);
        } else {
            r.setDependencia(form.getDependencia());
            r.setHorario(form.getHorario());
            r.setCanal(form.getCanal());
            r.setUrlCanal(form.getUrlCanal());
            r.setUrgente(form.isUrgente());
            limpiarCamposContenido(r);
        }

        r.reemplazarCategorias(resolverCategorias(form.getCategoriaIds()));

        if (r.getEstado() == EstadoPublicacion.PUBLICADO && r.getPublicadoEn() == null) {
            r.publicar(Instant.now());
        }
    }

    private Set<Categoria> resolverCategorias(Set<Long> ids) {
        Set<Categoria> resueltas = new LinkedHashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                categoriaRepository.findById(id).ifPresent(resueltas::add);
            }
        }
        return resueltas;
    }

    private void limpiarCamposRuta(Recurso r) {
        r.setDependencia(null);
        r.setHorario(null);
        r.setCanal(null);
        r.setUrlCanal(null);
        r.setUrgente(false);
    }

    private void limpiarCamposContenido(Recurso r) {
        r.setResumen(null);
        r.setCuerpo(null);
        r.setFuente(null);
        r.setSlug(null);
    }

    private String slugUnico(String titulo, Long idActual) {
        String base = Slugs.de(titulo);
        String slug = base;
        int n = 2;
        while (idActual == null
                ? recursoRepository.existsBySlug(slug)
                : recursoRepository.existsBySlugAndIdNot(slug, idActual)) {
            slug = base + "-" + n++;
        }
        return slug;
    }
}
