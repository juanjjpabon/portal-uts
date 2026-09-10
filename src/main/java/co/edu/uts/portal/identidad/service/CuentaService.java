package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import co.edu.uts.portal.identidad.web.dto.CambiarContrasenaForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void cambiarContrasena(Long usuarioId, CambiarContrasenaForm form) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontrado("Usuario " + usuarioId + " no existe"));

        if (!passwordEncoder.matches(form.getActual(), u.getHashContrasena())) {
            throw new OperacionInvalida("La contrasena actual no es correcta.");
        }
        if (!form.coincideConfirmacion()) {
            throw new OperacionInvalida("La nueva contrasena y su confirmacion no coinciden.");
        }
        if (passwordEncoder.matches(form.getNueva(), u.getHashContrasena())) {
            throw new OperacionInvalida("La nueva contrasena debe ser distinta de la actual.");
        }

        u.establecerContrasena(passwordEncoder.encode(form.getNueva()), false);
        bitacora.registrar(AccionBitacora.CONTRASENA_CAMBIADA, "Usuario", u.getId(),
                "El usuario " + u.getCorreo() + " cambio su contrasena");
    }
}
