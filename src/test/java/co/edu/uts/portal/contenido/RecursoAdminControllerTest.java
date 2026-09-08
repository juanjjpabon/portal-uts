package co.edu.uts.portal.contenido;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.service.CategoriaService;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.contenido.web.RecursoAdminController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * HU-18 / HU-19: el CRUD unico de recursos y su control de acceso (HU-17).
 */
@WebMvcTest(RecursoAdminController.class)
@Import(SecurityConfig.class)
class RecursoAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RecursoService recursoService;
    @MockitoBean
    CategoriaService categoriaService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalVeElListadoDeContenidos() throws Exception {
        when(recursoService.listar(any(), any(), any(), any())).thenReturn(List.of());
        when(categoriaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/admin/recursos/contenidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/recursos/lista"))
                .andExpect(model().attribute("tipo", co.edu.uts.portal.contenido.domain.TipoRecurso.CONTENIDO));
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void seccionDesconocidaDa404() throws Exception {
        mockMvc.perform(get("/admin/recursos/cualquiercosa"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoAccede() throws Exception {
        mockMvc.perform(get("/admin/recursos/contenidos"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(recursoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void crearContenidoInvalidoReRenderizaFormulario() throws Exception {
        mockMvc.perform(post("/admin/recursos/contenidos").with(csrf())
                        .param("titulo", "")           // titulo en blanco
                        .param("estado", "BORRADOR"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/recursos/formulario"));
        verifyNoInteractions(recursoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void eliminarRedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/recursos/rutas/7/eliminar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/recursos/rutas"));
        verify(recursoService).eliminar(eq(7L), eq(co.edu.uts.portal.contenido.domain.TipoRecurso.RUTA));
    }
}
