package co.edu.uts.portal.autoorientacion;

import co.edu.uts.portal.autoorientacion.service.AutoorientacionService;
import co.edu.uts.portal.autoorientacion.service.RespuestasIncompletas;
import co.edu.uts.portal.autoorientacion.web.AutoorientacionController;
import co.edu.uts.portal.autoorientacion.web.UrgenciaController;
import co.edu.uts.portal.autoorientacion.web.dto.CuestionarioParaResponder;
import co.edu.uts.portal.autoorientacion.web.dto.ResultadoAutoorientacion;
import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.service.PortalPublicoService;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import co.edu.uts.portal.parametros.service.ParametroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * M03: el flujo publico de autoorientacion (HU-08 a HU-12).
 */
@WebMvcTest({AutoorientacionController.class, UrgenciaController.class})
@Import(SecurityConfig.class)
class AutoorientacionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AutoorientacionService servicio;
    @MockitoBean
    ParametroService parametros;
    // Ajuste Laura #4: UrgenciaController ahora pide la lista de recursos urgentes
    // a PortalPublicoService (ver urgenciaEsPublica). Sin stub -> Mockito devuelve
    // lista vacia, que es exactamente el caso "sin recursos urgentes marcados".
    @MockitoBean
    PortalPublicoService portalPublicoService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    private CuestionarioParaResponder cuestionario() {
        return new CuestionarioParaResponder("tamizaje", "Tamizaje", "desc", 1,
                List.of(new CuestionarioParaResponder.PreguntaVista(10L, "P1",
                        List.of(new CuestionarioParaResponder.OpcionVista(100L, "A")))));
    }

    @Test
    void landingEsPublica() throws Exception {
        when(servicio.temasDisponibles()).thenReturn(List.of());
        mockMvc.perform(get("/autoorientacion")).andExpect(status().isOk());
    }

    @Test
    void urgenciaEsPublica() throws Exception {
        mockMvc.perform(get("/urgencia")).andExpect(status().isOk());
    }

    @Test
    void responderSinPasarPorElAvisoRedirigeAlAviso() throws Exception {
        mockMvc.perform(get("/autoorientacion/tamizaje/responder"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autoorientacion/tamizaje"));
    }

    @Test
    void responderTrasLeerElAvisoMuestraElFormulario() throws Exception {
        when(servicio.paraResponder("tamizaje")).thenReturn(cuestionario());
        mockMvc.perform(get("/autoorientacion/tamizaje/responder").param("aviso", "leido"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/autoorientacion/formulario"));
    }

    @Test
    void resultadoIncompletoVuelveAlFormulario() throws Exception {
        when(servicio.calcular(eq("tamizaje"), eq(1), anyMap()))
                .thenThrow(new RespuestasIncompletas(List.of(10L)));
        when(servicio.paraResponder("tamizaje")).thenReturn(cuestionario());

        mockMvc.perform(post("/autoorientacion/tamizaje/resultado").with(csrf())
                        .param("numeroVersion", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/autoorientacion/formulario"));
    }

    @Test
    void resultadoCompletoMuestraElNivel() throws Exception {
        when(servicio.calcular(eq("tamizaje"), eq(1), anyMap())).thenReturn(
                new ResultadoAutoorientacion("Alto", "#c00", "busca apoyo", "no es diagnostico",
                        List.of("Habla con bienestar"), List.of()));
        when(servicio.tema("tamizaje")).thenReturn(new co.edu.uts.portal.cuestionario.domain.Cuestionario("Tamizaje"));
        when(parametros.valor(any(), any())).thenReturn("x");

        mockMvc.perform(post("/autoorientacion/tamizaje/resultado").with(csrf())
                        .param("numeroVersion", "1")
                        .param("respuestas[10]", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/autoorientacion/resultado"));
    }
}
