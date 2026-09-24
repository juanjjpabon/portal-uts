package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Rol;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.RolRepository;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import co.edu.uts.portal.identidad.web.dto.CrearUsuarioForm;
import co.edu.uts.portal.identidad.web.dto.EditarUsuarioForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion de usuarios y roles (HU-21), exclusiva de ADMIN_TECNICO (HU-17, segunda
 * capa sobre la regla de URL). Cada cambio queda en la bitacora (HU-22).
 *
 * Reglas anti-bloqueo: nadie se desactiva ni se quita ADMIN_TECNICO a si mismo, y
 * siempre debe quedar al menos un ADMIN_TECNICO activo.
 */
@Service
@PreAuthorize("hasRole('ADMIN_TECNICO')")
public class UsuarioService {

    private static final String OBJ = "Usuario";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraService bitacora;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          PasswordEncoder passwordEncoder, BitacoraService bitacora) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.bitacora = bitacora;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNombreCompletoAsc();
    }

    /**
     * Ajuste Laura #6: conteo para el panel. Hereda el @PreAuthorize de la clase
     * (solo ADMIN_TECNICO), igual que el resto de este servicio.
     */
    @Transactional(readOnly = true)
    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontrado("Usuario " + id + " no existe"));
    }

    @Transactional
    public Usuario crear(CrearUsuarioForm form) {
        String correo = form.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new OperacionInvalida("Ya existe un usuario con el correo " + correo + ".");
        }
        Usuario u = new Usuario(form.getNombreCompleto().trim(), correo,
                passwordEncoder.encode(form.getContrasena()));
        u.establecerContrasena(u.getHashContrasena(), true);   // debe cambiarla al ingresar
        for (NombreRol nr : form.getRoles()) {
            u.agregarRol(rol(nr));
        }
        usuarioRepository.save(u);

        bitacora.registrar(AccionBitacora.USUARIO_CREADO, OBJ, u.getId(),
                "Creó el usuario " + correo
                        + (form.getRoles().isEmpty() ? " sin roles" : " con rol(es) " + nombres(form.getRoles())));
        return u;
    }

    @Transactional
    public void actualizarDatos(Long id, EditarUsuarioForm form) {
        Usuario u = obtener(id);
        String correo = form.getCorreo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new OperacionInvalida("El correo " + correo + " ya lo usa otro usuario.");
                });
        u.setNombreCompleto(form.getNombreCompleto().trim());
        u.setCorreo(correo);
        bitacora.registrar(AccionBitacora.USUARIO_ACTUALIZADO, OBJ, id,
                "Actualizó los datos de " + correo);
    }

    @Transactional
    public void fijarRoles(Long id, Set<NombreRol> nuevos) {
        Usuario u = obtener(id);
        Set<NombreRol> actuales = EnumSet.noneOf(NombreRol.class);
        u.getRoles().forEach(r -> actuales.add(r.getNombre()));

        Set<NombreRol> aAgregar = EnumSet.noneOf(NombreRol.class);
        aAgregar.addAll(nuevos);
        aAgregar.removeAll(actuales);

        Set<NombreRol> aQuitar = EnumSet.noneOf(NombreRol.class);
        aQuitar.addAll(actuales);
        aQuitar.removeAll(nuevos);

        if (aQuitar.contains(NombreRol.ADMIN_TECNICO)) {
            if (esUsuarioActual(u)) {
                throw new OperacionInvalida("No puedes quitarte a ti mismo el rol de administrador técnico.");
            }
            if (u.isActivo() && usuarioRepository.contarActivosConRol(NombreRol.ADMIN_TECNICO) <= 1) {
                throw new OperacionInvalida("Debe quedar al menos un administrador técnico activo.");
            }
        }

        for (NombreRol nr : aAgregar) {
            u.agregarRol(rol(nr));
            bitacora.registrar(AccionBitacora.ROL_ASIGNADO, OBJ, id,
                    "Asignó el rol " + nr.name() + " a " + u.getCorreo());
        }
        for (NombreRol nr : aQuitar) {
            rolRepository.findByNombre(nr).ifPresent(u::quitarRol);
            bitacora.registrar(AccionBitacora.ROL_REVOCADO, OBJ, id,
                    "Revocó el rol " + nr.name() + " a " + u.getCorreo());
        }
    }

    @Transactional
    public void cambiarActivo(Long id, boolean activo) {
        Usuario u = obtener(id);
        if (u.isActivo() == activo) {
            return;
        }
        if (!activo) {
            if (esUsuarioActual(u)) {
                throw new OperacionInvalida("No puedes desactivar tu propia cuenta.");
            }
            if (u.tieneRol(NombreRol.ADMIN_TECNICO)
                    && usuarioRepository.contarActivosConRol(NombreRol.ADMIN_TECNICO) <= 1) {
                throw new OperacionInvalida("Debe quedar al menos un administrador técnico activo.");
            }
        }
        u.setActivo(activo);
        bitacora.registrar(activo ? AccionBitacora.USUARIO_ACTIVADO : AccionBitacora.USUARIO_DESACTIVADO,
                OBJ, id, (activo ? "Activó" : "Desactivó") + " la cuenta de " + u.getCorreo());
    }

    @Transactional
    public void restablecerContrasena(Long id, String nueva) {
        Usuario u = obtener(id);
        u.establecerContrasena(passwordEncoder.encode(nueva), true);
        bitacora.registrar(AccionBitacora.CONTRASENA_RESTABLECIDA, OBJ, id,
                "Restableció la contraseña de " + u.getCorreo());
    }

    private Rol rol(NombreRol nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new OperacionInvalida("El rol " + nombre + " no está configurado."));
    }

    private boolean esUsuarioActual(Usuario u) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof UsuarioAutenticado p
                && p.getId() != null && p.getId().equals(u.getId());
    }

    private String nombres(Set<NombreRol> roles) {
        return roles.stream().map(Enum::name).sorted().reduce((a, b) -> a + ", " + b).orElse("");
    }
}
