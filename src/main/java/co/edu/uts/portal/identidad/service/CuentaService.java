package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import co.edu.uts.portal.identidad.web.dto.CambiarContrasenaForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Autoservicio de la propia cuenta (HU-21): cualquier usuario autenticado cambia
 * su contrasena. Se usa tanto para el cambio voluntario como para el obligatorio
 * del primer ingreso.
 */
@Service
public class CuentaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraService bitacora;

    public CuentaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                         BitacoraService bitacora) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.bitacora = bitacora;
    }

    /**
     * @param requiereActual false para el cambio obligatorio del primer ingreso
     *                       (debeCambiarClave): el usuario ya uso esa contrasena para
     *                       autenticarse un paso antes, volver a pedirla es redundante.
     *                       true para el cambio voluntario desde el perfil, donde si
     *                       hace falta verificarla.
     */
    @Transactional
    public void cambiarContrasena(Long usuarioId, CambiarContrasenaForm form, boolean requiereActual) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontrado("Usuario " + usuarioId + " no existe"));

        if (requiereActual
                && (!StringUtils.hasText(form.getActual())
                    || !passwordEncoder.matches(form.getActual(), u.getHashContrasena()))) {
            throw new OperacionInvalida("La contraseña actual no es correcta.");
        }
        if (!form.coincideConfirmacion()) {
            throw new OperacionInvalida("La nueva contraseña y su confirmación no coinciden.");
        }
        if (passwordEncoder.matches(form.getNueva(), u.getHashContrasena())) {
            throw new OperacionInvalida("La nueva contraseña debe ser distinta de la actual.");
        }

        u.establecerContrasena(passwordEncoder.encode(form.getNueva()), false);
        bitacora.registrar(AccionBitacora.CONTRASENA_CAMBIADA, "Usuario", u.getId(),
                "El usuario " + u.getCorreo() + " cambió su contraseña");
    }
}
