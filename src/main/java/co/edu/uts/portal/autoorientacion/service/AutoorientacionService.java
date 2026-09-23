package co.edu.uts.portal.autoorientacion.service;

import co.edu.uts.portal.autoorientacion.web.dto.CuestionarioParaResponder;
import co.edu.uts.portal.autoorientacion.web.dto.ResultadoAutoorientacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.EstadoVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.repository.CuestionarioRepository;
import co.edu.uts.portal.cuestionario.repository.NivelResultadoRepository;
import co.edu.uts.portal.parametros.service.ParametroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * M03: sirve el contenido de la autoorientacion publica y calcula el resultado
 * usando la version PUBLICADA del cuestionario (HU-10). No persiste ni deja rastro
 * individual (HU-13): no toca BitacoraService ni guarda ninguna respuesta. La unica
 * escritura es el contador agregado de HU-23 (NivelResultado.vecesObtenido), que no
 * distingue quien respondio -- es una fila de configuracion compartida del
 * cuestionario, no un registro por persona.
 */
@Service
public class AutoorientacionService {

    private static final String AVISO_NO_DIAGNOSTICO_DEFAULT =
            "Este resultado es orientativo y no constituye un diagnóstico.";

    private final CuestionarioRepository cuestionarioRepository;
    private final NivelResultadoRepository nivelResultadoRepository;
    private final CalculadoraNivel calculadora;
    private final ParametroService parametros;

    public AutoorientacionService(CuestionarioRepository cuestionarioRepository,
                                  NivelResultadoRepository nivelResultadoRepository,
                                  CalculadoraNivel calculadora, ParametroService parametros) {
        this.cuestionarioRepository = cuestionarioRepository;
        this.nivelResultadoRepository = nivelResultadoRepository;
        this.calculadora = calculadora;
        this.parametros = parametros;
    }

    @Transactional(readOnly = true)
    public List<Cuestionario> temasDisponibles() {
        return cuestionarioRepository.findByActivoTrueAndVersiones_EstadoOrderByNombreAsc(EstadoVersion.PUBLICADA);
    }

    /** Cuestionario activo y con version publicada; 404 si no. Para el aviso previo (HU-08). */
    @Transactional(readOnly = true)
    public Cuestionario tema(String slug) {
        Cuestionario c = cuestionarioRepository.findBySlug(slug)
                .filter(Cuestionario::isActivo)
                .orElseThrow(() -> new AutoorientacionNoDisponible("Tema no disponible: " + slug));
        if (c.versionPublicada().isEmpty()) {
            throw new AutoorientacionNoDisponible("El tema " + slug + " no tiene version publicada.");
        }
        return c;
    }

    @Transactional(readOnly = true)
    public CuestionarioParaResponder paraResponder(String slug) {
        CuestionarioVersion v = versionPublicada(slug);
        List<CuestionarioParaResponder.PreguntaVista> preguntas = new ArrayList<>();
        for (var p : v.getPreguntas()) {
            List<CuestionarioParaResponder.OpcionVista> ops = p.getOpciones().stream()
                    .map(o -> new CuestionarioParaResponder.OpcionVista(o.getId(), o.getTexto()))
                    .toList();
            preguntas.add(new CuestionarioParaResponder.PreguntaVista(p.getId(), p.getEnunciado(), ops));
        }
        Cuestionario c = v.getCuestionario();
        return new CuestionarioParaResponder(c.getSlug(), c.getNombre(), c.getDescripcion(),
                v.getNumero(), preguntas);
    }

    @Transactional
    public ResultadoAutoorientacion calcular(String slug, int numeroVersion, Map<Long, Long> respuestas) {
        CuestionarioVersion v = versionPublicada(slug);
        if (v.getNumero() != numeroVersion) {
            throw new VersionObsoleta("El cuestionario se actualizo; empieza de nuevo.");
        }
        validar(v, respuestas);

        NivelResultado nivel = calculadora.nivelPara(v, respuestas);
        nivelResultadoRepository.incrementarVecesObtenido(nivel.getId());   // HU-23: solo el contador agregado

        List<String> recomendaciones = nivel.getRecomendaciones().stream()
                .map(r -> r.getTexto()).toList();
        List<ResultadoAutoorientacion.RutaVista> rutas = nivel.getRutas().stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(this::aRutaVista).toList();

        return new ResultadoAutoorientacion(
                nivel.getNombre(), nivel.getColor(), nivel.getExplicacion(),
                parametros.valor("aviso.autoorientacion.no_diagnostico", AVISO_NO_DIAGNOSTICO_DEFAULT),
                recomendaciones, rutas);
    }

    private void validar(CuestionarioVersion v, Map<Long, Long> respuestas) {
        List<Long> faltantes = new ArrayList<>();
        for (var p : v.getPreguntas()) {
            Long opcionId = respuestas.get(p.getId());
            boolean valida = opcionId != null
                    && p.getOpciones().stream().anyMatch(o -> o.getId().equals(opcionId));
            if (!valida) {
                faltantes.add(p.getId());
            }
        }
        if (!faltantes.isEmpty()) {
            throw new RespuestasIncompletas(faltantes);
        }
    }

    private ResultadoAutoorientacion.RutaVista aRutaVista(Recurso r) {
        return new ResultadoAutoorientacion.RutaVista(r.getId(), r.getTitulo(), r.getDependencia(),
                r.getHorario(), r.getCanal(), r.getUrlCanal());
    }

    private CuestionarioVersion versionPublicada(String slug) {
        Cuestionario c = cuestionarioRepository.findBySlug(slug)
                .filter(Cuestionario::isActivo)
                .orElseThrow(() -> new AutoorientacionNoDisponible("Tema no disponible: " + slug));
        CuestionarioVersion v = c.versionPublicada()
                .orElseThrow(() -> new AutoorientacionNoDisponible("El tema " + slug + " no tiene version publicada."));
        v.getPreguntas().forEach(p -> p.getOpciones().size());
        v.getNiveles().forEach(n -> {
            n.getRecomendaciones().size();
            n.getRutas().size();
        });
        return v;
    }
}
