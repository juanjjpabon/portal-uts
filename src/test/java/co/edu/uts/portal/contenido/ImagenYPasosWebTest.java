package co.edu.uts.portal.contenido;

import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.domain.Imagen;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.service.ImagenService;
import co.edu.uts.portal.contenido.service.PasoRutaService;
import co.edu.uts.portal.contenido.web.PasoRutaAdminController;
import co.edu.uts.portal.contenido.web.publico.ImagenPublicaController;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Reunion con la directora (22/9/2026): imagenes publicas y pasos de ruta en el panel,
 * con el mismo control de acceso que el resto del CRUD (HU-17).
 */
@WebMvcTest({ImagenPublicaController.class, PasoRutaAdminController.class})
@Import(SecurityConfig.class)
class ImagenYPasosWebTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ImagenService imagenService;
    @MockitoBean
    PasoRutaService pasoRutaService;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    private Recurso ruta(long id) {
        Recurso r = new Recurso(TipoRecurso.RUTA);
        ReflectionTestUtils.setField(r, "id", id);
        r.setTitulo("Tutorías");
        return r;
    }

    // ------------------------------------------------------------- /imagenes/{id}

    @Test
    void lasImagenesSonPublicasYSeCacheanUnAno() throws Exception {
        UUID id = UUID.randomUUID();
        Imagen img = mock(Imagen.class);
        when(img.getId()).thenReturn(id);
        when(img.getTipoContenido()).thenReturn("image/png");
        when(img.getDatos()).thenReturn(new byte[]{1, 2, 3});
        when(imagenService.obtener(id)).thenReturn(Optional.of(img));

        mockMvc.perform(get("/imagenes/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=31536000")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void imagenInexistenteDa404() throws Exception {
        when(imagenService.obtener(any())).thenReturn(Optional.empty());
        mockMvc.perform(get("/imagenes/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    // ----------------------------------------------------- panel: pasos de la ruta

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void funcionalVeLosPasosDeLaRuta() throws Exception {
        when(pasoRutaService.ruta(5L)).thenReturn(ruta(5));
        when(pasoRutaService.listar(5L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/recursos/rutas/5/pasos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/recursos/pasos"))
                .andExpect(model().attributeExists("ruta", "pasos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_TECNICO")
    void tecnicoNoGestionaPasos() throws Exception {
        mockMvc.perform(get("/admin/recursos/rutas/5/pasos")).andExpect(status().isForbidden());
    }

    @Test
    void sinSesionSeRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/admin/recursos/rutas/5/pasos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void agregarPasoValidoVuelveALaLista() throws Exception {
        when(pasoRutaService.ruta(5L)).thenReturn(ruta(5));

        mockMvc.perform(multipart("/admin/recursos/rutas/5/pasos").with(csrf())
                        .param("titulo", "Escribe al correo del CAE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/recursos/rutas/5/pasos"));
        verify(pasoRutaService).crear(eq(5L), any(), isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void unArchivoQueNoEsImagenMuestraElErrorEnElFormulario() throws Exception {
        when(pasoRutaService.ruta(5L)).thenReturn(ruta(5));
        MockMultipartFile falso = new MockMultipartFile("imagenArchivo", "falso.jpg", "image/jpeg",
                "esto no es una imagen".getBytes());

        mockMvc.perform(multipart("/admin/recursos/rutas/5/pasos").file(falso).with(csrf())
                        .param("titulo", "Paso con foto"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/recursos/paso-formulario"))
                .andExpect(model().attributeHasFieldErrors("form", "imagenArchivo"));
        verify(pasoRutaService, never()).crear(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void pasoSinTituloNoSeGuarda() throws Exception {
        when(pasoRutaService.ruta(5L)).thenReturn(ruta(5));

        mockMvc.perform(post("/admin/recursos/rutas/5/pasos").with(csrf()).param("titulo", " "))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "titulo"));
        verify(pasoRutaService, never()).crear(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN_FUNCIONAL")
    void subirYBajarVuelvenALaLista() throws Exception {
        when(pasoRutaService.ruta(5L)).thenReturn(ruta(5));

        mockMvc.perform(post("/admin/recursos/rutas/5/pasos/2/subir").with(csrf()))
                .andExpect(redirectedUrl("/admin/recursos/rutas/5/pasos"));
        mockMvc.perform(post("/admin/recursos/rutas/5/pasos/2/bajar").with(csrf()))
                .andExpect(redirectedUrl("/admin/recursos/rutas/5/pasos"));
        verify(pasoRutaService).mover(5L, 2L, -1);
        verify(pasoRutaService).mover(5L, 2L, 1);
    }
}
