package co.edu.uts.portal.contenido.domain.validacion;

import co.edu.uts.portal.contenido.domain.TipoRecurso;

/**
 * Vista minima de un recurso para {@link RecursoCoherenteValidator}. La implementa
 * el DTO del formulario, de modo que la regla de coherencia por tipo vive en el
 * dominio pero se valida sobre la entrada del panel.
 */
public interface RecursoValidable {

    TipoRecurso getTipo();

    String getResumen();

    String getCuerpo();

    String getDependencia();

    String getCanal();

    String getHorario();

    boolean tieneCategorias();
}
