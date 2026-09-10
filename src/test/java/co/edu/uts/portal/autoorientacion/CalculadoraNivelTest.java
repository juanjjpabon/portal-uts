package co.edu.uts.portal.autoorientacion;

import co.edu.uts.portal.autoorientacion.service.CalculadoraNivel;
import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Opcion;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-10: con respuestas de prueba conocidas, el sistema produce el nivel esperado.
 */
class CalculadoraNivelTest {

    private final CalculadoraNivel calc = new CalculadoraNivel();
    private final AtomicLong seq = new AtomicLong(1);
    private CuestionarioVersion version;
    private long p1o0, p1o2, p2o0, p2o3;

    @BeforeEach
    void armarVersion() {
        Cuestionario c = new Cuestionario("Tamizaje");
        ReflectionTestUtils.setField(c, "id", 1L);
        version = c.nuevaVersionVacia();

        Pregunta p1 = version.agregarPregunta("Pregunta 1");
        p1o0 = idDe(p1.agregarOpcion("Nunca", 0));
        p1o2 = idDe(p1.agregarOpcion("Siempre", 2));
        Pregunta p2 = version.agregarPregunta("Pregunta 2");
        p2o0 = idDe(p2.agregarOpcion("Nunca", 0));
        p2o3 = idDe(p2.agregarOpcion("Siempre", 3));
        idDe(p1);
        idDe(p2);
        // puntaje maximo posible = 5

        NivelResultado bajo = version.agregarNivel("Bajo");
        bajo.setPuntajeMin(0);
        bajo.setPuntajeMax(2);
        NivelResultado alto = version.agregarNivel("Alto");
        alto.setPuntajeMin(3);
        alto.setPuntajeMax(5);
    }

    private long idDe(Object entidad) {
        long id = seq.getAndIncrement();
        ReflectionTestUtils.setField(entidad, "id", id);
        return id;
    }

    private Map<Long, Long> respuestas(long... opcionIds) {
        Map<Long, Long> m = new HashMap<>();
        // asume el orden de preguntas segun se creo
        var preguntas = version.getPreguntas();
        for (int i = 0; i < opcionIds.length; i++) {
            m.put(preguntas.get(i).getId(), opcionIds[i]);
        }
        return m;
    }

    @Test
    void sumaLosPuntajesDeLasOpcionesElegidas() {
        assertThat(calc.puntaje(version, respuestas(p1o2, p2o3))).isEqualTo(5);
        assertThat(calc.puntaje(version, respuestas(p1o0, p2o0))).isEqualTo(0);
        assertThat(calc.puntaje(version, respuestas(p1o2, p2o0))).isEqualTo(2);
    }

    @Test
    void nivelBajoEnElLimiteSuperiorDeLaBanda() {
        assertThat(calc.nivelPara(version, respuestas(p1o2, p2o0)).getNombre()).isEqualTo("Bajo");
    }

    @Test
    void nivelAltoJustoSobreElLimite() {
        assertThat(calc.nivelPara(version, respuestas(p1o0, p2o3)).getNombre()).isEqualTo("Alto");
    }

    @Test
    void todoEnCeroDaElNivelMasBajo() {
        assertThat(calc.nivelPara(version, respuestas(p1o0, p2o0)).getNombre()).isEqualTo("Bajo");
    }

    @Test
    void maximoPuntajeDaElNivelMasAlto() {
        assertThat(calc.nivelPara(version, respuestas(p1o2, p2o3)).getNombre()).isEqualTo("Alto");
    }
}
