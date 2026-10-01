package co.edu.uts.portal.analitica;

import co.edu.uts.portal.analitica.service.AnaliticaService;
import co.edu.uts.portal.analitica.web.AnaliticaAdminController;
import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import co.edu.uts.portal.identidad.service.LimitadorIntentosLogin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * M07 (HU-23/HU-24): trasladado a trabajo futuro el 26/9/2026 (informe final, 4.1 y
 * 5.10). La ruta queda bloqueada en SecurityConfig para todo el mundo, incluido el
 * administrador funcional que antes sí podía entrar -- estas pruebas verifican que el
 * bloqueo es real a nivel de URL, no solo una afirmación del informe.
 */
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
    LimitadorIntentosLogin limitadorIntentosLogin;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalNoAccedeMientrasEsteEnTrabajoFuturo() throws Exception {
        mockMvc.perform(get("/admin/analitica")).andExpect(status().isForbidden());
        verifyNoInteractions(analiticaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccede() throws Exception {
        mockMvc.perform(get("/admin/analitica")).andExpect(status().isForbidden());
        verifyNoInteractions(analiticaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void exportarCsvTampocoEstaDisponible() throws Exception {
        mockMvc.perform(get("/admin/analitica/exportar.csv")).andExpect(status().isForbidden());
        verifyNoInteractions(analiticaService);
    }
}
