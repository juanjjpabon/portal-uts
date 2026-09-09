package co.edu.uts.portal.cuestionario.service;

import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.EstadoVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Opcion;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import co.edu.uts.portal.cuestionario.domain.Recomendacion;
import co.edu.uts.portal.cuestionario.repository.CuestionarioRepository;
import co.edu.uts.portal.cuestionario.web.dto.CuestionarioForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Cuestionarios y su ciclo de versiones (HU-20). Lecturas abiertas al panel;
 * mutaciones restringidas a ADMIN_FUNCIONAL (HU-17). Fecha y responsable via
 * BaseAuditable (HU-18).
 */
@Service
public class CuestionarioService {

    private final CuestionarioRepository cuestionarioRepository;
    private final ValidadorVersion validador;

    public CuestionarioService(CuestionarioRepository cuestionarioRepository, ValidadorVersion validador) {
        this.cuestionarioRepository = cuestionarioRepository;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public List<Cuestionario> listar() {
        List<Cuestionario> cs = cuestionarioRepository.findAllByOrderByNombreAsc();
        cs.forEach(c -> c.getVersiones().size());   // open-in-view=false: inicializar para la vista
        return cs;
    }

    @Transactional(readOnly = true)
    public Cuestionario obtener(Long id) {
        return cuestionarioRepository.findById(id)
                .orElseThrow(() -> new CuestionarioNoEncontrado("Cuestionario " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public Cuestionario obtenerConVersiones(Long id) {
        Cuestionario c = obtener(id);
        c.getVersiones().size();
        return c;
    }

    @Transactional(readOnly = true)
    public CuestionarioVersion obtenerVersion(Long cuestionarioId, int numero) {
        return obtener(cuestionarioId).version(numero)
                .orElseThrow(() -> new CuestionarioNoEncontrado(
                        "El cuestionario " + cuestionarioId + " no tiene la version " + numero));
    }

    /** Version con todo su arbol inicializado (preguntas/opciones, niveles/recomendaciones/rutas). */
    @Transactional(readOnly = true)
    public CuestionarioVersion obtenerVersionCompleta(Long cuestionarioId, int numero) {
        CuestionarioVersion v = obtenerVersion(cuestionarioId, numero);
        v.getPreguntas().forEach(p -> p.getOpciones().size());
        v.getNiveles().forEach(n -> {
            n.getRecomendaciones().size();
            n.getRutas().size();
        });
        return v;
    }

    /** Version cargada y verificada como editable; usada por los controladores de preguntas/niveles. */
    @Transactional(readOnly = true)
    public CuestionarioVersion obtenerVersionEditable(Long cuestionarioId, int numero) {
        CuestionarioVersion v = obtenerVersion(cuestionarioId, numero);
        if (!v.esEditable()) {
            throw new VersionNoEditable("La version " + numero + " esta " + v.getEstado().getEtiqueta().toLowerCase()
                    + " y no se puede modificar. Crea una version nueva.");
        }
        return v;
    }

    @Transactional(readOnly = true)
    public List<String> problemasParaPublicar(Long cuestionarioId, int numero) {
        return validador.problemas(obtenerVersion(cuestionarioId, numero));
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public Cuestionario crear(CuestionarioForm form) {
        Cuestionario c = new Cuestionario(form.getNombre().trim());
        c.setDescripcion(form.getDescripcion());
        c.setActivo(form.isActivo());
        c.nuevaVersionVacia();               // version 1, BORRADOR
        return cuestionarioRepository.save(c);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizarDatos(Long id, CuestionarioForm form) {
        Cuestionario c = obtener(id);
        c.setNombre(form.getNombre().trim());
        c.setDescripcion(form.getDescripcion());
        c.setActivo(form.isActivo());
    }

    /** "Un cambio genera una nueva version": copia la ultima version a un BORRADOR nuevo. */
    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public int crearNuevaVersion(Long cuestionarioId) {
        Cuestionario c = obtener(cuestionarioId);
        CuestionarioVersion origen = c.ultimaVersion()
                .orElseThrow(() -> new CuestionarioNoEncontrado("El cuestionario no tiene versiones"));
        if (origen.esEditable()) {
            throw new VersionNoEditable("Ya existe una version en borrador (v" + origen.getNumero() + ").");
        }
        CuestionarioVersion destino = c.nuevaVersionVacia();
        copiarContenido(origen, destino);
        cuestionarioRepository.save(c);
        return destino.getNumero();
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void publicar(Long cuestionarioId, int numero) {
        Cuestionario c = obtener(cuestionarioId);
        CuestionarioVersion v = c.version(numero)
                .orElseThrow(() -> new CuestionarioNoEncontrado("Version inexistente"));
        if (v.getEstado() != EstadoVersion.BORRADOR) {
            throw new VersionNoEditable("Solo se publica una version en borrador.");
        }
        List<String> problemas = validador.problemas(v);
        if (!problemas.isEmpty()) {
            throw new VersionNoEditable("La version tiene " + problemas.size()
                    + " problema(s) sin resolver; corrige antes de publicar.");
        }
        c.versionPublicada().ifPresent(CuestionarioVersion::archivar);
        v.publicar(Instant.now());
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void archivar(Long cuestionarioId, int numero) {
        CuestionarioVersion v = obtenerVersion(cuestionarioId, numero);
        if (v.getEstado() != EstadoVersion.PUBLICADA) {
            throw new VersionNoEditable("Solo se archiva la version publicada.");
        }
        v.archivar();
    }

    private void copiarContenido(CuestionarioVersion origen, CuestionarioVersion destino) {
        destino.setNotasVersion(origen.getNotasVersion());

        for (Pregunta po : origen.getPreguntas()) {
            Pregunta pd = destino.agregarPregunta(po.getEnunciado());
            pd.setOrden(po.getOrden());
            pd.setObligatoria(po.isObligatoria());
            for (Opcion oo : po.getOpciones()) {
                Opcion od = pd.agregarOpcion(oo.getTexto(), oo.getValor());
                od.setOrden(oo.getOrden());
            }
        }
        for (NivelResultado no : origen.getNiveles()) {
            NivelResultado nd = destino.agregarNivel(no.getNombre());
            nd.setOrden(no.getOrden());
            nd.setPuntajeMin(no.getPuntajeMin());
            nd.setPuntajeMax(no.getPuntajeMax());
            nd.setExplicacion(no.getExplicacion());
            nd.setColor(no.getColor());
            for (Recomendacion ro : no.getRecomendaciones()) {
                nd.agregarRecomendacion(ro.getTexto()).setOrden(ro.getOrden());
            }
            nd.reemplazarRutas(no.getRutas());   // las rutas (Recurso) se comparten, no se copian
        }
    }
}
