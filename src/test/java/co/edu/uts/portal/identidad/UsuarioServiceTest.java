package co.edu.uts.portal.identidad;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Rol;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.repository.RolRepository;
import co.edu.uts.portal.identidad.repository.UsuarioRepository;
import co.edu.uts.portal.identidad.service.OperacionInvalida;
import co.edu.uts.portal.identidad.service.UsuarioService;
import co.edu.uts.portal.identidad.web.dto.CrearUsuarioForm;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
    private final RolRepository rolRepo = mock(RolRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final UsuarioService service = new UsuarioService(usuarioRepo, rolRepo, encoder, bitacora);

    private Usuario usuario(long id, String correo, boolean activo, NombreRol... roles) {
        Usuario u = new Usuario("Nombre " + id, correo, "hash");
        ReflectionTestUtils.setField(u, "id", id);
        u.setActivo(activo);
        for (NombreRol r : roles) {
            u.agregarRol(new Rol(r));
        }
        return u;
    }

    @Test
    void crearHasheaLaClaveMarcaCambioObligatorioYAudita() {
        when(usuarioRepo.existsByCorreoIgnoreCase("ana@uts.edu.co")).thenReturn(false);
        when(encoder.encode("clave1234")).thenReturn("HASH");
        when(rolRepo.findByNombre(NombreRol.ADMIN_FUNCIONAL)).thenReturn(Optional.of(new Rol(NombreRol.ADMIN_FUNCIONAL)));

        CrearUsuarioForm form = new CrearUsuarioForm();
        form.setNombreCompleto("Ana");
        form.setCorreo("Ana@uts.edu.co");
        form.setContrasena("clave1234");
        form.setRoles(Set.of(NombreRol.ADMIN_FUNCIONAL));

        Usuario creado = service.crear(form);

        assertThat(creado.getHashContrasena()).isEqualTo("HASH");
        assertThat(creado.isDebeCambiarClave()).isTrue();
        assertThat(creado.getCorreo()).isEqualTo("ana@uts.edu.co");
        verify(usuarioRepo).save(creado);
        verify(bitacora).registrar(eq(AccionBitacora.USUARIO_CREADO), eq("Usuario"), any(), any());
    }

    @Test
    void crearRechazaCorreoDuplicado() {
        when(usuarioRepo.existsByCorreoIgnoreCase("ana@uts.edu.co")).thenReturn(true);
        CrearUsuarioForm form = new CrearUsuarioForm();
        form.setNombreCompleto("Ana");
        form.setCorreo("ana@uts.edu.co");
        form.setContrasena("clave1234");

        assertThatThrownBy(() -> service.crear(form)).isInstanceOf(OperacionInvalida.class);
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void noSeDesactivaAlUltimoTecnicoActivo() {
        Usuario tecnico = usuario(1, "tec@uts.edu.co", true, NombreRol.ADMIN_TECNICO);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(tecnico));
        when(usuarioRepo.contarActivosConRol(NombreRol.ADMIN_TECNICO)).thenReturn(1L);

        assertThatThrownBy(() -> service.cambiarActivo(1L, false))
                .isInstanceOf(OperacionInvalida.class)
                .hasMessageContaining("administrador técnico activo");
        assertThat(tecnico.isActivo()).isTrue();
    }

    @Test
    void desactivaUnTecnicoSiHayOtroActivo() {
        Usuario tecnico = usuario(2, "tec2@uts.edu.co", true, NombreRol.ADMIN_TECNICO);
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(tecnico));
        when(usuarioRepo.contarActivosConRol(NombreRol.ADMIN_TECNICO)).thenReturn(2L);

        service.cambiarActivo(2L, false);

        assertThat(tecnico.isActivo()).isFalse();
        verify(bitacora).registrar(eq(AccionBitacora.USUARIO_DESACTIVADO), eq("Usuario"), eq(2L), any());
    }

    @Test
    void fijarRolesEmiteAsignacionYRevocacionSegunElDiff() {
        Usuario u = usuario(3, "u@uts.edu.co", true, NombreRol.ADMIN_FUNCIONAL);
        when(usuarioRepo.findById(3L)).thenReturn(Optional.of(u));
        when(rolRepo.findByNombre(NombreRol.ADMIN_TECNICO)).thenReturn(Optional.of(new Rol(NombreRol.ADMIN_TECNICO)));
        when(rolRepo.findByNombre(NombreRol.ADMIN_FUNCIONAL)).thenReturn(Optional.of(new Rol(NombreRol.ADMIN_FUNCIONAL)));

        service.fijarRoles(3L, Set.of(NombreRol.ADMIN_TECNICO));   // + TECNICO, - FUNCIONAL

        verify(bitacora).registrar(eq(AccionBitacora.ROL_ASIGNADO), eq("Usuario"), eq(3L), any());
        verify(bitacora).registrar(eq(AccionBitacora.ROL_REVOCADO), eq("Usuario"), eq(3L), any());
        assertThat(u.tieneRol(NombreRol.ADMIN_TECNICO)).isTrue();
        assertThat(u.tieneRol(NombreRol.ADMIN_FUNCIONAL)).isFalse();
    }

    @Test
    void noSeRevocaTecnicoAlUltimoActivo() {
        Usuario u = usuario(4, "u4@uts.edu.co", true, NombreRol.ADMIN_TECNICO);
        when(usuarioRepo.findById(4L)).thenReturn(Optional.of(u));
        when(usuarioRepo.contarActivosConRol(NombreRol.ADMIN_TECNICO)).thenReturn(1L);

        assertThatThrownBy(() -> service.fijarRoles(4L, Set.of()))
                .isInstanceOf(OperacionInvalida.class);
        assertThat(u.tieneRol(NombreRol.ADMIN_TECNICO)).isTrue();
    }
}
