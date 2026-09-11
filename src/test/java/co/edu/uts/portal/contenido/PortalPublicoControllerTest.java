package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.config.SecurityConfig;
import co.edu.uts.portal.contenido.service.ContenidoNoDisponible;
import co.edu.uts.portal.contenido.service.PortalPublicoService;
import co.edu.uts.portal.contenido.web.publico.BusquedaController;
import co.edu.uts.portal.contenido.web.publico.ContenidoPublicoController;
import co.edu.uts.portal.contenido.web.publico.RutaPublicoController;
import co.edu.uts.portal.identidad.service.DetalleUsuarioService;
import co.edu.uts.portal.identidad.service.RegistroAccesoHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * M01/M02: paginas publicas de contenidos, rutas y busqueda (HU-01 a HU-05), sin
 * autenticacion.
 */
@WebMvcTest({ContenidoPublicoController.class, RutaPublicoController.class, BusquedaController.class})
@Import(SecurityConfig.class)
class PortalPublicoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PortalPublicoService servicio;
    @MockitoBean
    RegistroAccesoHandler registroAccesoHandler;
    @MockitoBean
    DetalleUsuarioService detalleUsuarioService;

    @Test
    void listaDeContenidosEsPublica() throws Exception {
        when(servicio.contenidos(null)).thenReturn(List.of());
        when(servicio.categorias()).thenReturn(List.of());
        mockMvc.perform(get("/contenidos")).andExpect(status().isOk());
    }

    @Test
    void detalleDeContenidoInexistenteDa404() throws Exception {
        when(servicio.contenido("x")).thenThrow(new ContenidoNoDisponible("no"));
        mockMvc.perform(get("/contenidos/x")).andExpect(status().isNotFound());
    }

    @Test
    void detalleDeContenidoPublicadoSeVe() throws Exception {
        Recurso r = new Recurso(TipoRecurso.CONTENIDO);
        r.setTitulo("Senales de alerta");
        r.setSlug("senales-de-alerta");
        r.setResumen("resumen");
        r.setCuerpo("cuerpo");
        when(servicio.contenido("senales-de-alerta")).thenReturn(r);
        mockMvc.perform(get("/contenidos/senales-de-alerta")).andExpect(status().isOk());
    }

    @Test
    void listaDeRutasEsPublica() throws Exception {
        when(servicio.rutas()).thenReturn(List.of());
        when(servicio.contactos()).thenReturn(List.of());
        mockMvc.perform(get("/rutas")).andExpect(status().isOk());
    }

    @Test
    void busquedaSinTextoNoRompe() throws Exception {
        when(servicio.buscar(null)).thenReturn(List.of());
        mockMvc.perform(get("/buscar")).andExpect(status().isOk());
    }

    @Test
    void busquedaConTextoAgrupaPorTipo() throws Exception {
        Recurso r = new Recurso(TipoRecurso.CONTENIDO);
        r.setTitulo("Ansiedad");
        r.setSlug("ansiedad");
        when(servicio.buscar("ansiedad")).thenReturn(List.of(r));
        mockMvc.perform(get("/buscar").param("q", "ansiedad")).andExpect(status().isOk());
    }
}
