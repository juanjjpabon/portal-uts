package co.edu.uts.portal.autoorientacion.service;

import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Opcion;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

/**
 * HU-10: calcula el nivel orientativo sumando el puntaje de las opciones elegidas y
 * buscando la banda [puntajeMin, puntajeMax] que lo contiene. Funcion pura y
 * determinista: con respuestas conocidas produce siempre el mismo nivel.
 */
@Component
public class CalculadoraNivel {

    private static final Logger log = LoggerFactory.getLogger(CalculadoraNivel.class);

    /** Suma de los valores de las opciones elegidas (respuestas: preguntaId -> opcionId). */
    public int puntaje(CuestionarioVersion version, Map<Long, Long> respuestas) {
        int total = 0;
        for (Pregunta p : version.getPreguntas()) {
            Long opcionId = respuestas.get(p.getId());
            if (opcionId == null) {
                continue;
            }
            total += p.getOpciones().stream()
                    .filter(o -> o.getId().equals(opcionId))
                    .mapToInt(Opcion::getValor)
                    .findFirst()
                    .orElse(0);
        }
        return total;
    }

    public NivelResultado nivelPara(CuestionarioVersion version, Map<Long, Long> respuestas) {
        int total = puntaje(version, respuestas);
        return version.getNiveles().stream()
                .filter(n -> n.contiene(total))
                .findFirst()
                .orElseGet(() -> nivelMasCercano(version, total));
    }

    private NivelResultado nivelMasCercano(CuestionarioVersion version, int total) {
        log.warn("Ningun nivel de la version {} del cuestionario {} cubre el puntaje {}; se usa el mas cercano.",
                version.getNumero(), version.getCuestionario().getId(), total);
        return version.getNiveles().stream()
                .min(Comparator.comparingInt(n -> distancia(n, total)))
                .orElseThrow(() -> new IllegalStateException("La version publicada no tiene niveles."));
    }

    private int distancia(NivelResultado n, int total) {
        if (total < n.getPuntajeMin()) {
            return n.getPuntajeMin() - total;
        }
        if (total > n.getPuntajeMax()) {
            return total - n.getPuntajeMax();
        }
        return 0;
    }
}
