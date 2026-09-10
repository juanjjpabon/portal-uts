package co.edu.uts.portal.autoorientacion.web.dto;

import java.util.List;

/**
 * Resultado orientativo mostrado al visitante (HU-11 / HU-12). Se construye en
 * memoria y se descarta al terminar la peticion (HU-13).
 */
public record ResultadoAutoorientacion(
        String nivelNombre,
        String nivelColor,
        String explicacion,
        String avisoNoDiagnostico,
        List<String> recomendaciones,
        List<RutaVista> rutas) {

    public record RutaVista(String titulo, String dependencia, String horario,
                            String canal, String urlCanal) {
    }
}
