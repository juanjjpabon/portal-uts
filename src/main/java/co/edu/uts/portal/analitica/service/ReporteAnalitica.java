package co.edu.uts.portal.analitica.service;

import java.util.List;

/**
 * HU-23: reporte agregado. Cada lista ya viene filtrada por el umbral
 * anti-reidentificacion (>= 5) -- lo que llega aqui es lo que se puede mostrar.
 */
public record ReporteAnalitica(
        List<FilaContenido> contenidosMasConsultados,
        List<FilaNivel> autoorientacionPorNivel,
        List<FilaValoracion> valoraciones) {

    public boolean estaVacio() {
        return contenidosMasConsultados.isEmpty() && autoorientacionPorNivel.isEmpty() && valoraciones.isEmpty();
    }

    public record FilaContenido(String titulo, int vistas) {
    }

    public record FilaNivel(String cuestionario, String nivel, long veces) {
    }

    public record FilaValoracion(String titulo, String tipo, int util, int noUtil) {
    }
}
