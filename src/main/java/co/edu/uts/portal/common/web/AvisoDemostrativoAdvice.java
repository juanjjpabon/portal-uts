package co.edu.uts.portal.common.web;

import co.edu.uts.portal.parametros.service.ParametroService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Ajuste Laura #1: pone a disposicion de fragments/comunes.html :: navbarPublico
 * (incluido en toda pagina publica, incluida auth/login) el aviso configurable de
 * contenido demostrativo, sin que cada controlador publico tenga que pedirlo.
 *
 * Va como @ModelAttribute de un @ControllerAdvice global -- y no como una llamada
 * SpEL directa a un bean (p. ej. "@parametroService...") dentro de la plantilla --
 * porque esta ultima forma no la ve el mecanismo de @MockitoBean: en las pruebas
 * @WebMvcTest que recortan el contexto, el bean simplemente no existe con ese nombre
 * para el resolvedor de SpEL aunque el mock este declarado, y la plantilla revienta.
 * Con inyeccion normal (por tipo, via ObjectProvider) si funciona, y ademas se
 * degrada sola: en un @WebMvcTest que ni siquiera mockea ParametroService, el
 * ObjectProvider devuelve null y este metodo simplemente no pone aviso, en vez de
 * romper el arranque del contexto.
 */
@ControllerAdvice
public class AvisoDemostrativoAdvice {

    private final ObjectProvider<ParametroService> parametros;

    public AvisoDemostrativoAdvice(ObjectProvider<ParametroService> parametros) {
        this.parametros = parametros;
    }

    @ModelAttribute("avisoContenidoDemostrativo")
    public String avisoContenidoDemostrativo() {
        ParametroService servicio = parametros.getIfAvailable();
        return servicio == null ? "" : servicio.valor("aviso.contenido_demostrativo", "");
    }
}
