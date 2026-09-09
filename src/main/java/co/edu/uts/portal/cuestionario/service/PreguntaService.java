package co.edu.uts.portal.cuestionario.service;

import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.Opcion;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import co.edu.uts.portal.cuestionario.web.dto.OpcionForm;
import co.edu.uts.portal.cuestionario.web.dto.PreguntaForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Edicion de preguntas y opciones de una version BORRADOR (HU-20). Toda mutacion
 * pasa por {@link VersionAccessor}, que rechaza versiones no editables.
 */
@Service
@PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
public class PreguntaService {

    private final VersionAccessor acceso;

    public PreguntaService(VersionAccessor acceso) {
        this.acceso = acceso;
    }

    @Transactional
    public void agregar(Long cuestionarioId, int numero, PreguntaForm form) {
        Pregunta p = acceso.editable(cuestionarioId, numero).agregarPregunta(form.getEnunciado().trim());
        p.setObligatoria(form.isObligatoria());
        if (form.getOrden() > 0) {
            p.setOrden(form.getOrden());
        }
    }

    @Transactional
    public void actualizar(Long cuestionarioId, int numero, Long preguntaId, PreguntaForm form) {
        Pregunta p = pregunta(cuestionarioId, numero, preguntaId);
        p.setEnunciado(form.getEnunciado().trim());
        p.setOrden(form.getOrden());
        p.setObligatoria(form.isObligatoria());
    }

    @Transactional
    public void eliminar(Long cuestionarioId, int numero, Long preguntaId) {
        CuestionarioVersion v = acceso.editable(cuestionarioId, numero);
        v.getPreguntas().removeIf(p -> p.getId().equals(preguntaId));
    }

    @Transactional
    public void agregarOpcion(Long cuestionarioId, int numero, Long preguntaId, OpcionForm form) {
        Opcion o = pregunta(cuestionarioId, numero, preguntaId)
                .agregarOpcion(form.getTexto().trim(), form.getValor());
        if (form.getOrden() > 0) {
            o.setOrden(form.getOrden());
        }
    }

    @Transactional
    public void actualizarOpcion(Long cuestionarioId, int numero, Long preguntaId, Long opcionId, OpcionForm form) {
        Opcion o = opcion(cuestionarioId, numero, preguntaId, opcionId);
        o.setTexto(form.getTexto().trim());
        o.setValor(form.getValor());
        o.setOrden(form.getOrden());
    }

    @Transactional
    public void eliminarOpcion(Long cuestionarioId, int numero, Long preguntaId, Long opcionId) {
        pregunta(cuestionarioId, numero, preguntaId).getOpciones()
                .removeIf(o -> o.getId().equals(opcionId));
    }

    private Pregunta pregunta(Long cuestionarioId, int numero, Long preguntaId) {
        return acceso.editable(cuestionarioId, numero).getPreguntas().stream()
                .filter(p -> p.getId().equals(preguntaId))
                .findFirst()
                .orElseThrow(() -> new CuestionarioNoEncontrado("Pregunta " + preguntaId + " no existe en esta version"));
    }

    private Opcion opcion(Long cuestionarioId, int numero, Long preguntaId, Long opcionId) {
        return pregunta(cuestionarioId, numero, preguntaId).getOpciones().stream()
                .filter(o -> o.getId().equals(opcionId))
                .findFirst()
                .orElseThrow(() -> new CuestionarioNoEncontrado("Opcion " + opcionId + " no existe"));
    }
}
