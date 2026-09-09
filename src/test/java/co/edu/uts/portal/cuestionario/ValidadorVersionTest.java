package co.edu.uts.portal.cuestionario;

import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import co.edu.uts.portal.cuestionario.service.ValidadorVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-20: reglas que debe cumplir una version para poder publicarse.
 */
class ValidadorVersionTest {

    private final ValidadorVersion validador = new ValidadorVersion();

    private CuestionarioVersion versionBase() {
        Cuestionario c = new Cuestionario("Tamizaje");
        CuestionarioVersion v = c.nuevaVersionVacia();

        Pregunta p1 = v.agregarPregunta("Pregunta 1");
        p1.agregarOpcion("Nunca", 0);
        p1.agregarOpcion("Siempre", 2);
        Pregunta p2 = v.agregarPregunta("Pregunta 2");
        p2.agregarOpcion("Nunca", 0);
        p2.agregarOpcion("Siempre", 2);
        // puntaje maximo posible = 4
        return v;
    }

    private void nivel(CuestionarioVersion v, String nombre, int min, int max, boolean conReco, boolean conRuta) {
        NivelResultado n = v.agregarNivel(nombre);
        n.setPuntajeMin(min);
        n.setPuntajeMax(max);
        n.setExplicacion("explica " + nombre);
        if (conReco) {
            n.agregarRecomendacion("haz algo");
        }
        if (conRuta) {
            n.reemplazarRutas(java.util.Set.of(new co.edu.uts.portal.contenido.domain.Recurso(
                    co.edu.uts.portal.contenido.domain.TipoRecurso.RUTA)));
        }
    }

    @Test
    void versionCompletaNoTieneProblemas() {
        CuestionarioVersion v = versionBase();
        nivel(v, "Bajo", 0, 2, true, true);
        nivel(v, "Alto", 3, 4, true, true);

        assertThat(validador.problemas(v)).isEmpty();
    }

    @Test
    void detectaHuecoEntreNiveles() {
        CuestionarioVersion v = versionBase();
        nivel(v, "Bajo", 0, 1, true, true);
        nivel(v, "Alto", 3, 4, true, true);   // falta cubrir el 2

        assertThat(validador.problemas(v)).anyMatch(p -> p.toLowerCase().contains("hueco"));
    }

    @Test
    void detectaSolapamiento() {
        CuestionarioVersion v = versionBase();
        nivel(v, "Bajo", 0, 3, true, true);
        nivel(v, "Alto", 2, 4, true, true);

        assertThat(validador.problemas(v)).anyMatch(p -> p.toLowerCase().contains("solap"));
    }

    @Test
    void exigeRecomendacionYRutaPorNivel() {
        CuestionarioVersion v = versionBase();
        nivel(v, "Bajo", 0, 2, false, false);
        nivel(v, "Alto", 3, 4, true, true);

        List<String> p = validador.problemas(v);
        assertThat(p).anyMatch(s -> s.contains("recomendacion"));
        assertThat(p).anyMatch(s -> s.contains("ruta aplicable"));
    }

    @Test
    void exigeAlMenosDosOpcionesPorPregunta() {
        Cuestionario c = new Cuestionario("x");
        CuestionarioVersion v = c.nuevaVersionVacia();
        v.agregarPregunta("Sin opciones");
        nivel(v, "Bajo", 0, 0, true, true);
        nivel(v, "Alto", 1, 1, true, true);

        assertThat(validador.problemas(v)).anyMatch(s -> s.contains("2 opciones"));
    }
}
