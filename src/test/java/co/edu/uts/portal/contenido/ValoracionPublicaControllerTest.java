package co.edu.uts.portal.contenido;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.service.ValoracionService;
import co.edu.uts.portal.contenido.web.publico.ValoracionPublicaController;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-25: publica (sin login), redirige de vuelta segun el Referer validado contra
 * una lista blanca -- nunca a la URL cruda del encabezado.
 */
@WebMvcTest(ValoracionPublicaController.class)
@Import(SecurityConfig.class)
class ValoracionPublicaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ValoracionService valoracionService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    void esPublicaYDelegaElVotoAlServicio() throws Exception {
        mockMvc.perform(post("/valoraciones/9").with(csrf())
                        .param("util", "true")
                        .header("Referer", "http://localhost:8080/contenidos/senales-de-alerta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contenidos/senales-de-alerta?gracias=1"));

        verify(valoracionService).registrar(eq(9L), eq(true));
    }

    @Test
    void vuelveARutasCuandoElRefererEsElListado() throws Exception {
        mockMvc.perform(post("/valoraciones/9").with(csrf())
                        .param("util", "false")
                        .header("Referer", "http://localhost:8080/rutas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rutas?gracias=1"));
    }

    @Test
    void sinRefererONoReconocidoVuelveAlInicio() throws Exception {
        mockMvc.perform(post("/valoraciones/9").with(csrf()).param("util", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?gracias=1"));
    }

    @Test
    void unRefererAjenoNuncaSeUsaTalCual() throws Exception {
        mockMvc.perform(post("/valoraciones/9").with(csrf())
                        .param("util", "true")
                        .header("Referer", "https://sitio-ajeno.evil/loquesea"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?gracias=1"));
    }
}
