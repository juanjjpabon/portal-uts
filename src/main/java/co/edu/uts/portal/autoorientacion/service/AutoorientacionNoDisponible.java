package co.edu.uts.portal.autoorientacion.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** El tema no existe, no esta activo o no tiene una version publicada. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AutoorientacionNoDisponible extends RuntimeException {

    public AutoorientacionNoDisponible(String mensaje) {
        super(mensaje);
    }
}
