package co.edu.uts.portal.identidad;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import co.edu.uts.portal.identidad.service.CuentaService;
import co.edu.uts.portal.identidad.service.OperacionInvalida;
import co.edu.uts.portal.identidad.web.dto.CambiarContrasenaForm;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cambio obligatorio (debeCambiarClave) vs voluntario: solo el segundo exige la
 * contrasena actual -- en el obligatorio el usuario ya la uso para iniciar sesion.
 */
class CuentaServiceTest {

    private final UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final CuentaService service = new CuentaService(usuarioRepo, encoder, bitacora);

    private Usuario usuario() {
        Usuario u = new Usuario("Nombre", "u@uts.edu.co", "hash-actual");
        ReflectionTestUtils.setField(u, "id", 1L);
        return u;
    }

    private CambiarContrasenaForm form(String actual, String nueva, String confirmacion) {
        CambiarContrasenaForm f = new CambiarContrasenaForm();
        f.setActual(actual);
        f.setNueva(nueva);
        f.setConfirmacion(confirmacion);
        return f;
    }

    @Test
    void cambioObligatorioNoExigeLaContrasenaActual() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario()));
        // "actual" viene null (el formulario ni siquiera pide el campo) y aun asi funciona:
        // la unica comparacion contra el hash existente es la de "nueva != actual".
        when(encoder.matches("nueva12345", "hash-actual")).thenReturn(false);
        when(encoder.encode("nueva12345")).thenReturn("hash-nuevo");

        service.cambiarContrasena(1L, form(null, "nueva12345", "nueva12345"), false);

        verify(bitacora).registrar(eq(AccionBitacora.CONTRASENA_CAMBIADA), any(), any(), any());
    }

    @Test
    void cambioVoluntarioExigeQueLaContrasenaActualCoincida() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario()));
        when(encoder.matches("mala", "hash-actual")).thenReturn(false);

        assertThatThrownBy(() -> service.cambiarContrasena(1L, form("mala", "nueva12345", "nueva12345"), true))
                .isInstanceOf(OperacionInvalida.class)
                .hasMessageContaining("contraseña actual");
    }

    @Test
    void cambioVoluntarioConContrasenaActualCorrectaFunciona() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario()));
        when(encoder.matches("actual123", "hash-actual")).thenReturn(true);
        when(encoder.matches("nueva12345", "hash-actual")).thenReturn(false);
        when(encoder.encode("nueva12345")).thenReturn("hash-nuevo");

        service.cambiarContrasena(1L, form("actual123", "nueva12345", "nueva12345"), true);
    }

    @Test
    void cambioObligatorioIgualExigeQueLasDosNuevasCoincidan() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario()));

        assertThatThrownBy(() -> service.cambiarContrasena(1L, form(null, "nueva12345", "otra1234"), false))
                .isInstanceOf(OperacionInvalida.class)
                .hasMessageContaining("no coinciden");
    }
}
