package co.edu.uts.portal.identidad;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.service.CategoriaService;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import co.edu.uts.portal.cuestionario.service.NivelService;
import co.edu.uts.portal.cuestionario.service.PreguntaService;
import co.edu.uts.portal.parametros.service.ParametroService;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-16 (login/logout) y HU-17 (permisos por rol, separacion estricta) a nivel de URL.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class SeguridadWebTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;

    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @MockitoBean
    RecursoService recursoService;

    @MockitoBean
    CategoriaService categoriaService;

    @MockitoBean
    ParametroService parametroService;

    @MockitoBean
    CuestionarioService cuestionarioService;

    @MockitoBean
    PreguntaService preguntaService;

    @MockitoBean
    NivelService nivelService;

    @Test
    void loginEsPublico() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void panelExigeAutenticacion() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalNoAccedeAGestionDeUsuarios() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccedeAGestionDeContenido() throws Exception {
        mockMvc.perform(get("/admin/recursos/contenidos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoAccedeAGestionDeUsuarios() throws Exception {
        // No hay controlador aun: pasa la autorizacion y cae en 404, nunca en 403.
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutInvalidaLaSesion() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
