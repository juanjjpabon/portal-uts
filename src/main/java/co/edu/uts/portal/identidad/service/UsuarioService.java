package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Rol;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.RolRepository;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de usuarios y roles (HU-21). Segunda capa de HU-17: aunque la URL ya
 * exige ROLE_ADMIN_TECNICO, cada operacion sensible lo vuelve a exigir a nivel de
 * metodo, de modo que ninguna otra ruta pueda invocarla por error.
 *
 * La auditoria de cada cambio (HU-21/HU-22) se agregara con el modulo de bitacora.
 */
@Service
@PreAuthorize("hasRole('ADMIN_TECNICO')")
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void asignarRol(Long usuarioId, NombreRol nombreRol) {
        Usuario usuario = obtener(usuarioId);
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol inexistente: " + nombreRol));
        usuario.agregarRol(rol);
    }

    @Transactional
    public void revocarRol(Long usuarioId, NombreRol nombreRol) {
        Usuario usuario = obtener(usuarioId);
        rolRepository.findByNombre(nombreRol).ifPresent(usuario::quitarRol);
    }

    @Transactional
    public void cambiarActivo(Long usuarioId, boolean activo) {
        obtener(usuarioId).setActivo(activo);
    }

    private Usuario obtener(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario inexistente: " + usuarioId));
    }
}
