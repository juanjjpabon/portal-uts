package co.edu.uts.portal.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Paginas de error con lenguaje claro y sin filtrar informacion sensible
 * (adelanto de HU-26). La denegacion por rol (HU-17) llega aqui via accessDeniedPage.
 */
@Controller
public class ErrorPersonalizadoController {

    @GetMapping("/error/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado() {
        return "error/403";
    }
}
