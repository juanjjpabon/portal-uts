package co.edu.uts.portal.common;

import co.edu.uts.portal.common.web.PortalErrorAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-26: lo que llega a /error nunca debe traer detalle tecnico; un 5xx real si debe
 * quedar identificable para buscarlo en el log (referencia).
 */
class PortalErrorAttributesTest {

    private final PortalErrorAttributes atributos = new PortalErrorAttributes();

    private ServletWebRequest peticionConError(int status, Exception excepcion) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/algo");
        request.setAttribute("jakarta.servlet.error.status_code", status);
        request.setAttribute("jakarta.servlet.error.request_uri", "/algo");
        if (excepcion != null) {
            request.setAttribute("jakarta.servlet.error.exception", excepcion);
        }
        return new ServletWebRequest(request);
    }

    @Test
    void unErrorTecnicoAgregaUnaReferenciaYQuedaSinDetalleTecnico() {
        Map<String, Object> resultado = atributos.getErrorAttributes(
                peticionConError(500, new RuntimeException("credenciales invalidas de la BD: secret123")),
                ErrorAttributeOptions.defaults());

        assertThat(resultado).containsKey("referencia");
        assertThat(resultado.get("referencia")).asString().hasSize(8);
        assertThat(resultado).doesNotContainKeys("trace", "exception", "message", "errors");
    }

    @Test
    void un404NoGeneraReferencia() {
        Map<String, Object> resultado = atributos.getErrorAttributes(
                peticionConError(404, null), ErrorAttributeOptions.defaults());

        assertThat(resultado).doesNotContainKey("referencia");
    }

    @Test
    void un403NoGeneraReferencia() {
        Map<String, Object> resultado = atributos.getErrorAttributes(
                peticionConError(403, null), ErrorAttributeOptions.defaults());

        assertThat(resultado).doesNotContainKey("referencia");
    }
}
