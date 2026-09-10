package co.edu.uts.portal.identidad;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Usuario;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import co.edu.uts.portal.identidad.service.UsuarioService;
import co.edu.uts.portal.identidad.web.UsuarioAdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-21: gestion de usuarios exclusiva de ADMIN_TECNICO (HU-17).
 */
@WebMvcTest(UsuarioAdminController.class)
@Import(SecurityConfig.class)
class UsuarioAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService usuarioService;
    @MockitoBean
    BitacoraService bitacoraService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoVeElListado() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/admin/usuarios")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalNoAccede() throws Exception {
        mockMvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
        verifyNoInteractions(usuarioService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void crearRedirigeAlDetalle() throws Exception {
        Usuario u = new Usuario("Ana", "ana@uts.edu.co", "h");
        ReflectionTestUtils.setField(u, "id", 5L);
        when(usuarioService.crear(any())).thenReturn(u);

        mockMvc.perform(post("/admin/usuarios").with(csrf())
                        .param("nombreCompleto", "Ana")
                        .param("correo", "ana@uts.edu.co")
                        .param("contrasena", "clave1234")
                        .param("roles", "ADMIN_FUNCIONAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios/5"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void fijarRolesDelegaEnElServicio() throws Exception {
        mockMvc.perform(post("/admin/usuarios/5/roles").with(csrf())
                        .param("roles", "ADMIN_TECNICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios/5"));
        verify(usuarioService).fijarRoles(eq(5L), eq(Set.of(NombreRol.ADMIN_TECNICO)));
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void desactivarDelegaEnElServicio() throws Exception {
        mockMvc.perform(post("/admin/usuarios/5/desactivar").with(csrf()))
                .andExpect(status().is3xxRedirection());
        verify(usuarioService).cambiarActivo(eq(5L), eq(false));
    }
}
