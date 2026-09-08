package co.edu.uts.portal.parametros.web.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recoge los valores editados del formulario unico de HU-27, indexados por id de
 * parametro (inputs {@code valores[<id>]}).
 */
public class ParametrosForm {

    private Map<Long, String> valores = new LinkedHashMap<>();

    public Map<Long, String> getValores() {
        return valores;
    }

    public void setValores(Map<Long, String> valores) {
        this.valores = valores;
    }
}
