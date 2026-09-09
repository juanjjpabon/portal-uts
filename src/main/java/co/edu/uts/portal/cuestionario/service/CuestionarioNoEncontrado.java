package co.edu.uts.portal.cuestionario.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CuestionarioNoEncontrado extends RuntimeException {

    public CuestionarioNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
