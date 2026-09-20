package co.edu.uts.portal.cuestionario.service;

import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import co.edu.uts.portal.cuestionario.domain.Pregunta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comprueba que una version este completa y coherente antes de publicarla (HU-20).
 * Devuelve la lista de problemas; vacia = se puede publicar.
 */
@Component
public class ValidadorVersion {

    public List<String> problemas(CuestionarioVersion v) {
        List<String> p = new ArrayList<>();

        if (v.getPreguntas().isEmpty()) {
            p.add("La versión no tiene preguntas.");
        }
        for (Pregunta pr : v.getPreguntas()) {
            if (pr.getOpciones().size() < 2) {
                p.add("La pregunta \"" + recorta(pr.getEnunciado()) + "\" necesita al menos 2 opciones.");
            }
        }

        if (v.getNiveles().size() < 2) {
            p.add("Define al menos 2 niveles de resultado.");
        }
        for (NivelResultado n : v.getNiveles()) {
            if (n.getPuntajeMin() > n.getPuntajeMax()) {
                p.add("El nivel \"" + n.getNombre() + "\" tiene el puntaje mínimo mayor que el máximo.");
            }
            if (n.getExplicacion() == null || n.getExplicacion().isBlank()) {
                p.add("El nivel \"" + n.getNombre() + "\" no tiene explicación (HU-11).");
            }
            if (n.getRecomendaciones().isEmpty()) {
                p.add("El nivel \"" + n.getNombre() + "\" necesita al menos una recomendación (HU-12).");
            }
            if (n.getRutas().isEmpty()) {
                p.add("El nivel \"" + n.getNombre() + "\" necesita al menos una ruta aplicable (HU-12).");
            }
        }

        p.addAll(problemasDeCobertura(v));
        return p;
    }

    /** Los niveles deben cubrir 0..puntajeMaximoPosible sin huecos ni solapamientos. */
    private List<String> problemasDeCobertura(CuestionarioVersion v) {
        List<String> p = new ArrayList<>();
        if (v.getNiveles().size() < 2 || v.getPreguntas().isEmpty()) {
            return p;
        }
        int maxPosible = v.getPreguntas().stream().mapToInt(Pregunta::valorMaximo).sum();

        List<NivelResultado> ordenados = v.getNiveles().stream()
                .sorted(Comparator.comparingInt(NivelResultado::getPuntajeMin))
                .toList();

        if (ordenados.get(0).getPuntajeMin() > 0) {
            p.add("Ningún nivel cubre el puntaje 0.");
        }
        NivelResultado ultimo = ordenados.get(ordenados.size() - 1);
        if (ultimo.getPuntajeMax() < maxPosible) {
            p.add("Ningún nivel cubre el puntaje máximo posible (" + maxPosible + ").");
        }
        for (int i = 1; i < ordenados.size(); i++) {
            int finAnterior = ordenados.get(i - 1).getPuntajeMax();
            int inicioActual = ordenados.get(i).getPuntajeMin();
            if (inicioActual > finAnterior + 1) {
                p.add("Hay un hueco de puntaje entre \"" + ordenados.get(i - 1).getNombre()
                        + "\" y \"" + ordenados.get(i).getNombre() + "\".");
            } else if (inicioActual <= finAnterior) {
                p.add("Los niveles \"" + ordenados.get(i - 1).getNombre() + "\" y \""
                        + ordenados.get(i).getNombre() + "\" se solapan en el puntaje.");
            }
        }
        return p;
    }

    private String recorta(String s) {
        return s != null && s.length() > 40 ? s.substring(0, 40) + "..." : s;
    }
}
