package co.edu.uts.portal.autoorientacion.service;

import java.util.List;

/**
 * El envio no responde todas las preguntas o incluye una opcion que no pertenece a
 * su pregunta (HU-09). El controlador vuelve a mostrar el formulario con lo ya
 * marcado y el detalle de lo que falta.
 */
public class RespuestasIncompletas extends RuntimeException {

    private final List<Long> preguntasSinResponder;

    public RespuestasIncompletas(List<Long> preguntasSinResponder) {
        super("Faltan " + preguntasSinResponder.size() + " respuesta(s)");
        this.preguntasSinResponder = List.copyOf(preguntasSinResponder);
    }

    public List<Long> getPreguntasSinResponder() {
        return preguntasSinResponder;
    }
}
