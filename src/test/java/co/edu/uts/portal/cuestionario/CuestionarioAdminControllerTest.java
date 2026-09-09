package co.edu.uts.portal.cuestionario;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import co.edu.uts.portal.cuestionario.web.CuestionarioAdminController;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
 * HU-20: cuestionarios y workflow de versiones a nivel web (HU-17 incluido).
 */
@WebMvcTest(CuestionarioAdminController.class)
@Import(SecurityConfig.class)
class CuestionarioAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CuestionarioService cuestionarioService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalVeElListado() throws Exception {
        when(cuestionarioService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/admin/cuestionarios")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccede() throws Exception {
        mockMvc.perform(get("/admin/cuestionarios")).andExpect(status().isForbidden());
        verifyNoInteractions(cuestionarioService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void crearRedirigeAlEditorDeLaVersion1() throws Exception {
        Cuestionario c = new Cuestionario("Tamizaje");
        org.springframework.test.util.ReflectionTestUtils.setField(c, "id", 9L);
        when(cuestionarioService.crear(any())).thenReturn(c);

        mockMvc.perform(post("/admin/cuestionarios").with(csrf())
                        .param("nombre", "Tamizaje"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cuestionarios/9/v/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void publicarDelegaEnElServicio() throws Exception {
        mockMvc.perform(post("/admin/cuestionarios/9/v/2/publicar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cuestionarios/9"));
        verify(cuestionarioService).publicar(eq(9L), eq(2));
    }
}
