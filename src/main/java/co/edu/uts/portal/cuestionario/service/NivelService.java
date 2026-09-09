package co.edu.uts.portal.cuestionario.service;

import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.web.dto.NivelForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Edicion de niveles de resultado, recomendaciones y rutas de una version BORRADOR
 * (HU-20, HU-12). Toda mutacion pasa por {@link VersionAccessor}.
 */
@Service
@PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
public class NivelService {

    private final VersionAccessor acceso;
    private final RecursoRepository recursoRepository;

    public NivelService(VersionAccessor acceso, RecursoRepository recursoRepository) {
        this.acceso = acceso;
        this.recursoRepository = recursoRepository;
    }

    @Transactional
    public void agregar(Long cuestionarioId, int numero, NivelForm form) {
        NivelResultado n = acceso.editable(cuestionarioId, numero).agregarNivel(form.getNombre().trim());
        aplicar(n, form);
    }

    @Transactional
    public void actualizar(Long cuestionarioId, int numero, Long nivelId, NivelForm form) {
        NivelResultado n = nivel(cuestionarioId, numero, nivelId);
        n.setNombre(form.getNombre().trim());
        aplicar(n, form);
    }

    @Transactional
    public void eliminar(Long cuestionarioId, int numero, Long nivelId) {
        acceso.editable(cuestionarioId, numero).getNiveles().removeIf(n -> n.getId().equals(nivelId));
    }

    @Transactional
    public void agregarRecomendacion(Long cuestionarioId, int numero, Long nivelId, String texto) {
        nivel(cuestionarioId, numero, nivelId).agregarRecomendacion(texto.trim());
    }

    @Transactional
    public void actualizarRecomendacion(Long cuestionarioId, int numero, Long nivelId, Long recomendacionId,
                                        String texto, int orden) {
        nivel(cuestionarioId, numero, nivelId).getRecomendaciones().stream()
                .filter(r -> r.getId().equals(recomendacionId))
                .findFirst()
                .ifPresent(r -> {
                    r.setTexto(texto.trim());
                    r.setOrden(orden);
                });
    }

    @Transactional
    public void eliminarRecomendacion(Long cuestionarioId, int numero, Long nivelId, Long recomendacionId) {
        nivel(cuestionarioId, numero, nivelId).getRecomendaciones()
                .removeIf(r -> r.getId().equals(recomendacionId));
    }

    /** Reemplaza el conjunto de rutas del nivel con los recursos indicados (solo tipo RUTA). */
    @Transactional
    public void asignarRutas(Long cuestionarioId, int numero, Long nivelId, Set<Long> recursoIds) {
        NivelResultado n = nivel(cuestionarioId, numero, nivelId);
        Set<Recurso> rutas = new LinkedHashSet<>();
        if (recursoIds != null) {
            for (Long id : recursoIds) {
                recursoRepository.findById(id)
                        .filter(r -> r.getTipo() == TipoRecurso.RUTA)
                        .ifPresent(rutas::add);
            }
        }
        n.reemplazarRutas(rutas);
    }

    private void aplicar(NivelResultado n, NivelForm form) {
        n.setOrden(form.getOrden());
        n.setPuntajeMin(form.getPuntajeMin());
        n.setPuntajeMax(form.getPuntajeMax());
        n.setExplicacion(form.getExplicacion().trim());
        n.setColor(form.getColor());
    }

    private NivelResultado nivel(Long cuestionarioId, int numero, Long nivelId) {
        CuestionarioVersion v = acceso.editable(cuestionarioId, numero);
        return v.getNiveles().stream()
                .filter(n -> n.getId().equals(nivelId))
                .findFirst()
                .orElseThrow(() -> new CuestionarioNoEncontrado("Nivel " + nivelId + " no existe en esta version"));
    }
}
