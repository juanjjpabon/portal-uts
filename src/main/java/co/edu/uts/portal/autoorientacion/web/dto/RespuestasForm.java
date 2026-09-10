package co.edu.uts.portal.autoorientacion.web.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respuestas del formulario publico, indexadas por id de pregunta (inputs
 * {@code respuestas[<preguntaId>]}). Vive solo durante la peticion: no se guarda
 * en sesion ni en base de datos (HU-13).
 */
public class RespuestasForm {

    private int numeroVersion;

    private Map<Long, Long> respuestas = new LinkedHashMap<>();

    public int getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(int numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public Map<Long, Long> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(Map<Long, Long> respuestas) {
        this.respuestas = respuestas;
    }
}
