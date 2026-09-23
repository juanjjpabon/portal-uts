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

    public record RutaVista(Long id, String titulo, String dependencia, String horario,
                            String canal, String urlCanal) {

        /** Contacto directo listo para mostrar (dato visible + copiar), o null. Ver CanalDirecto. */
        public co.edu.uts.portal.contenido.domain.CanalDirecto getCanalDirecto() {
            return co.edu.uts.portal.contenido.domain.CanalDirecto.desde(urlCanal).orElse(null);
        }
    }
}
