package co.edu.uts.portal.autoorientacion.web.dto;

import java.util.List;

/**
 * Modelo de lectura del formulario publico (HU-09). Solo lo que el visitante necesita
 * ver: NO incluye el puntaje (valor) de cada opcion.
 */
public record CuestionarioParaResponder(
        String slug,
        String nombre,
        String descripcion,
        int numeroVersion,
        List<PreguntaVista> preguntas) {

    public record PreguntaVista(Long id, String enunciado, List<OpcionVista> opciones) {
    }

    public record OpcionVista(Long id, String texto) {
    }
}
