package co.edu.uts.portal.contenido.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** El contenido no existe o no esta en estado PUBLICADO (nunca se distingue cual, HU-01). */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContenidoNoDisponible extends RuntimeException {

    public ContenidoNoDisponible(String mensaje) {
        super(mensaje);
    }
}
