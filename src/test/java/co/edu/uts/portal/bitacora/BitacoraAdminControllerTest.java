package co.edu.uts.portal.bitacora;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.bitacora.web.BitacoraAdminController;
import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-22: visor global de bitacora, exclusivo de ADMIN_TECNICO (HU-17).
 */
@WebMvcTest(BitacoraAdminController.class)
@Import(SecurityConfig.class)
class BitacoraAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BitacoraService bitacoraService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoVeElVisor() throws Exception {
        when(bitacoraService.buscar(any(), any(), any(), any())).thenReturn(Page.empty());
        when(bitacoraService.tiposObjeto()).thenReturn(List.of());

        mockMvc.perform(get("/admin/bitacora")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalNoAccede() throws Exception {
        mockMvc.perform(get("/admin/bitacora")).andExpect(status().isForbidden());
        verifyNoInteractions(bitacoraService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void filtroPorTipoObjetoLlegaAlServicio() throws Exception {
        when(bitacoraService.buscar(any(), any(), any(), any())).thenReturn(Page.empty());
        when(bitacoraService.tiposObjeto()).thenReturn(List.of());

        mockMvc.perform(get("/admin/bitacora").param("tipoObjeto", "Usuario"))
                .andExpect(status().isOk());

        verify(bitacoraService).buscar(eq("Usuario"), isNull(), isNull(), any());
    }
}
