package co.edu.uts.portal.analitica;

import co.edu.uts.portal.analitica.service.AnaliticaService;
import co.edu.uts.portal.analitica.service.ReporteAnalitica;
import co.edu.uts.portal.analitica.web.AnaliticaAdminController;
import co.edu.uts.portal.config.SecurityConfig;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU-23/HU-24: solo ADMIN_FUNCIONAL ve o exporta las estadisticas (HU-17). */
@WebMvcTest(AnaliticaAdminController.class)
@Import(SecurityConfig.class)
class AnaliticaAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AnaliticaService analiticaService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalVeElReporte() throws Exception {
        when(analiticaService.generarReporte())
                .thenReturn(new ReporteAnalitica(List.of(), List.of(), List.of()));
        mockMvc.perform(get("/admin/analitica")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccede() throws Exception {
        mockMvc.perform(get("/admin/analitica")).andExpect(status().isForbidden());
        verifyNoInteractions(analiticaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void exportaComoCsvDescargable() throws Exception {
        when(analiticaService.generarCsv()).thenReturn("seccion,nombre,indicador,valor\r\n");
        mockMvc.perform(get("/admin/analitica/exportar.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }
}
