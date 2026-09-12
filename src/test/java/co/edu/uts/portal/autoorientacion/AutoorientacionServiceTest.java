package co.edu.uts.portal.autoorientacion;

import co.edu.uts.portal.autoorientacion.service.AutoorientacionNoDisponible;
import co.edu.uts.portal.autoorientacion.service.AutoorientacionService;
import co.edu.uts.portal.autoorientacion.service.CalculadoraNivel;
import co.edu.uts.portal.autoorientacion.service.RespuestasIncompletas;
import co.edu.uts.portal.autoorientacion.service.VersionObsoleta;
import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import co.edu.uts.portal.cuestionario.repository.CuestionarioRepository;
import co.edu.uts.portal.cuestionario.repository.NivelResultadoRepository;
import co.edu.uts.portal.parametros.service.ParametroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoorientacionServiceTest {

    private final CuestionarioRepository repo = mock(CuestionarioRepository.class);
    private final NivelResultadoRepository nivelRepo = mock(NivelResultadoRepository.class);
    private final ParametroService parametros = mock(ParametroService.class);
    private final AutoorientacionService service =
            new AutoorientacionService(repo, nivelRepo, new CalculadoraNivel(), parametros);

    private final AtomicLong seq = new AtomicLong(1);
    private Cuestionario cuestionario;
    private long op1, op2;

    @BeforeEach
    void datos() {
        cuestionario = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(cuestionario, "id", 1L);
        CuestionarioVersion v = cuestionario.nuevaVersionVacia();
        Pregunta p = v.agregarPregunta("Como te sientes");
        op1 = id(p.agregarOpcion("Bien", 0));
        op2 = id(p.agregarOpcion("Mal", 3));
        id(p);
        NivelResultado bajo = v.agregarNivel("Bajo");
        bajo.setPuntajeMin(0);
        bajo.setPuntajeMax(1);
        bajo.setExplicacion("estas bien");
        bajo.agregarRecomendacion("Sigue asi");
        id(bajo);
        NivelResultado alto = v.agregarNivel("Alto");
        alto.setPuntajeMin(2);
        alto.setPuntajeMax(3);
        alto.setExplicacion("busca apoyo");
        alto.agregarRecomendacion("Habla con bienestar");
        id(alto);
        v.publicar(Instant.now());

        when(parametros.valor(any(), any())).thenReturn("aviso");
    }

    private long id(Object o) {
        long id = seq.getAndIncrement();
        ReflectionTestUtils.setField(o, "id", id);
        return id;
    }

    private Map<Long, Long> resp(long opcionId) {
        Map<Long, Long> m = new HashMap<>();
        m.put(cuestionario.ultimaVersion().get().getPreguntas().get(0).getId(), opcionId);
        return m;
    }

    @Test
    void sinVersionPublicadaNoEstaDisponible() {
        Cuestionario soloBorrador = new Cuestionario("Otro");
        soloBorrador.nuevaVersionVacia();
        when(repo.findBySlug("otro")).thenReturn(Optional.of(soloBorrador));

        assertThatThrownBy(() -> service.paraResponder("otro"))
                .isInstanceOf(AutoorientacionNoDisponible.class);
    }

    @Test
    void versionEnviadaDistintaDeLaPublicadaEsObsoleta() {
        when(repo.findBySlug("tamizaje")).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> service.calcular("tamizaje", 99, resp(op1)))
                .isInstanceOf(VersionObsoleta.class);
    }

    @Test
    void respuestaFaltanteRebota() {
        when(repo.findBySlug("tamizaje")).thenReturn(Optional.of(cuestionario));

        assertThatThrownBy(() -> service.calcular("tamizaje", 1, new HashMap<>()))
                .isInstanceOf(RespuestasIncompletas.class);
    }

    @Test
    void calculaElNivelYSusRecomendacionesYRutas() {
        when(repo.findBySlug("tamizaje")).thenReturn(Optional.of(cuestionario));

        var r = service.calcular("tamizaje", 1, resp(op2));   // puntaje 3 -> Alto

        assertThat(r.nivelNombre()).isEqualTo("Alto");
        assertThat(r.explicacion()).isEqualTo("busca apoyo");
        assertThat(r.recomendaciones()).containsExactly("Habla con bienestar");

        // HU-23: solo se incrementa el contador agregado del nivel obtenido, nada mas.
        Long idNivelAlto = cuestionario.ultimaVersion().get().getNiveles().stream()
                .filter(n -> n.getNombre().equals("Alto")).findFirst().orElseThrow().getId();
        verify(nivelRepo).incrementarVecesObtenido(idNivelAlto);
    }

    @Test
    void paraResponderNoExponeElPuntajeDeLasOpciones() {
        when(repo.findBySlug("tamizaje")).thenReturn(Optional.of(cuestionario));

        var dto = service.paraResponder("tamizaje");

        assertThat(dto.preguntas()).hasSize(1);
        var opcion = dto.preguntas().get(0).opciones().get(0);
        // OpcionVista solo tiene id y texto: ningun campo "valor"
        assertThat(opcion.getClass().getRecordComponents()).extracting(java.lang.reflect.RecordComponent::getName)
                .containsExactlyInAnyOrder("id", "texto");
    }
}
