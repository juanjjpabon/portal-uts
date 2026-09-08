package co.edu.uts.portal.parametros;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import co.edu.uts.portal.parametros.service.ParametroService;
import co.edu.uts.portal.parametros.web.ParametroAdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-27: edicion de parametros operativos y su control de acceso (HU-17).
 */
@WebMvcTest(ParametroAdminController.class)
@Import(SecurityConfig.class)
class ParametroAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ParametroService parametroService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalVeElFormulario() throws Exception {
        when(parametroService.agrupados()).thenReturn(Map.of());

        mockMvc.perform(get("/admin/parametros"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccede() throws Exception {
        mockMvc.perform(get("/admin/parametros"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(parametroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void guardarDelegaEnElServicioYRedirige() throws Exception {
        when(parametroService.guardar(anyMap())).thenReturn(1);

        mockMvc.perform(post("/admin/parametros").with(csrf())
                        .param("valores[5]", "Nuevo texto de urgencia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/parametros"));

        verify(parametroService).guardar(Map.of(5L, "Nuevo texto de urgencia"));
    }
}
