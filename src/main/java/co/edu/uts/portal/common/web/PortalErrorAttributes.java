package co.edu.uts.portal.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.UUID;

/**
 * HU-26: intercepta todo lo que pasa por /error (tanto los 404/403 esperados como
 * cualquier excepcion no controlada). Nunca deja llegar al usuario un stack trace ni
 * un mensaje interno -- eso ya lo bloquea server.error.include-* en application.yml;
 * aqui, ademas:
 *
 *  - para un error tecnico real (status >= 500) se genera un codigo de referencia
 *    corto y se registra en el log, a nivel ERROR, la excepcion completa junto con
 *    el metodo y la ruta de la peticion -- nunca los parametros del formulario, para
 *    no dejar contrasenas en el log (p. ej. /login, /cuenta/contrasena);
 *  - el codigo de referencia se agrega al modelo para que la vista lo pueda mostrar,
 *    de forma que un usuario pueda reportar el problema sin exponer nada tecnico.
 */
@Component
public class PortalErrorAttributes extends DefaultErrorAttributes {

    private static final Logger log = LoggerFactory.getLogger(PortalErrorAttributes.class);

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> atributos = super.getErrorAttributes(webRequest, options);

        Object status = atributos.get("status");
        if (status instanceof Integer codigo && codigo >= 500) {
            String referencia = UUID.randomUUID().toString().substring(0, 8);
            Throwable error = getError(webRequest);
            String metodo = ((ServletWebRequest) webRequest).getRequest().getMethod();
            // "path" viene del atributo original de la peticion (jakarta.servlet.error.request_uri);
            // request.getRequestURI() aqui daria "/error", porque ya se hizo el forward interno.
            Object ruta = atributos.get("path");

            // Solo metodo + ruta + la excepcion (para el stack trace); nunca parametros
            // ni encabezados de la peticion, para no dejar contrasenas en el log.
            log.error("Error interno [{}] {} {}", referencia, metodo, ruta, error);

            atributos.put("referencia", referencia);
        }
        return atributos;
    }
}
