package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Transaccion separada del manejador HTTP para actualizar el ultimo acceso del
 * usuario tras un login exitoso (HU-16).
 */
@Service
public class RegistroAccesoService {

    private final UsuarioRepository usuarioRepository;

    public RegistroAccesoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarAcceso(Long usuarioId, Instant momento) {
        usuarioRepository.findById(usuarioId)
                .ifPresent(usuario -> usuario.registrarAcceso(momento));
    }
}
