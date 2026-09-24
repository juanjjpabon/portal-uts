package co.edu.uts.portal.contenido;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.PasoRuta;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.PasoRutaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.service.ImagenService;
import co.edu.uts.portal.contenido.service.OperacionNoPermitida;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-22: cada mutacion de Recurso queda en la bitacora.
 */
class RecursoServiceTest {

    private final RecursoRepository recursoRepo = mock(RecursoRepository.class);
    private final CategoriaRepository categoriaRepo = mock(CategoriaRepository.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final ImagenService imagenService = mock(ImagenService.class);
    private final PasoRutaRepository pasoRepo = mock(PasoRutaRepository.class);
    private final RecursoService service = new RecursoService(recursoRepo, categoriaRepo, bitacora, imagenService, pasoRepo);

    private Recurso recurso(long id, TipoRecurso tipo) {
        Recurso r = new Recurso(tipo);
        ReflectionTestUtils.setField(r, "id", id);
        r.setTitulo("Titulo");
        return r;
    }

    private RecursoForm formRuta() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.RUTA);
        f.setTitulo("Bienestar");
        f.setDependencia("Bienestar");
        f.setHorario("L-V");
        f.setCanal("123");
        f.setEstado(EstadoPublicacion.BORRADOR);
        f.setCategoriaIds(Set.of());
        return f;
    }

    @Test
    void contactoDirectoSeGuardaNormalizadoYLosOpcionalesVaciosComoNull() {
        RecursoForm f = formRuta();
        f.setUrlCanal(" cae@correo.uts.edu.co ");
        f.setHorario("   ");

        Recurso r = service.crear(f);

        assertThat(r.getUrlCanal()).isEqualTo("mailto:cae@correo.uts.edu.co");
        assertThat(r.getHorario()).isNull();
    }

    @Test
    void crearConImagenGuardaLaImagenYDejaSuId() {
        java.util.UUID id = java.util.UUID.randomUUID();
        co.edu.uts.portal.contenido.domain.Imagen guardada = mock(co.edu.uts.portal.contenido.domain.Imagen.class);
        when(guardada.getId()).thenReturn(id);
        co.edu.uts.portal.contenido.domain.ImagenProcesada procesada =
                new co.edu.uts.portal.contenido.domain.ImagenProcesada("image/jpeg", new byte[]{1}, 10, 10, "f.jpg");
        when(imagenService.guardar(procesada)).thenReturn(guardada);
        RecursoForm f = formRuta();
        f.setDestacado(true);
        f.setImagenAlt("  Folleto de tutorías ");

        Recurso r = service.crear(f, procesada);

        assertThat(r.getImagenId()).isEqualTo(id);
        assertThat(r.isDestacado()).isTrue();
        assertThat(r.getImagenAlt()).isEqualTo("Folleto de tutorías");
    }

    @Test
    void quitarLaImagenLaDesasociaYLimpiaLasHuerfanas() {
        Recurso existente = recurso(6, TipoRecurso.RUTA);
        existente.setImagenId(java.util.UUID.randomUUID());
        when(recursoRepo.findById(6L)).thenReturn(Optional.of(existente));
        RecursoForm f = formRuta();
        f.setQuitarImagen(true);

        service.actualizar(6L, f);

        assertThat(existente.getImagenId()).isNull();
        verify(imagenService).eliminarHuerfanas();
    }

    @Test
    void crearRegistraRecursoCreado() {
        service.crear(formRuta());
        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_CREADO), eq("Recurso"), any(), any());
    }

    @Test
    void actualizarRegistraRecursoActualizado() {
        when(recursoRepo.findById(1L)).thenReturn(Optional.of(recurso(1, TipoRecurso.RUTA)));
        service.actualizar(1L, formRuta());
        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_ACTUALIZADO), eq("Recurso"), eq(1L), any());
    }

    @Test
    void publicarRegistraRecursoPublicado() {
        when(recursoRepo.findById(2L)).thenReturn(Optional.of(recurso(2, TipoRecurso.RUTA)));
        when(pasoRepo.findByRutaIdOrderByOrdenAscIdAsc(2L)).thenReturn(List.of(new PasoRuta(2L, 1)));

        service.publicar(2L, TipoRecurso.RUTA);

        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_PUBLICADO), eq("Recurso"), eq(2L), any());
    }

    // Ajuste Laura #2: una ruta sin pasos no dice que hacer, no se puede publicar.
    @Test
    void publicarRutaSinPasosLanzaOperacionNoPermitidaYNoRegistraNada() {
        when(recursoRepo.findById(20L)).thenReturn(Optional.of(recurso(20, TipoRecurso.RUTA)));
        // pasoRepo sin stub -> Mockito devuelve lista vacia (sin pasos)

        assertThatThrownBy(() -> service.publicar(20L, TipoRecurso.RUTA))
                .isInstanceOf(OperacionNoPermitida.class);

        verify(bitacora, never()).registrar(eq(AccionBitacora.RECURSO_PUBLICADO), any(), any(), any());
    }

    // Un contenido o contacto no tiene pasos; la regla no le aplica.
    @Test
    void publicarContenidoNoRequierePasos() {
        when(recursoRepo.findById(21L)).thenReturn(Optional.of(recurso(21, TipoRecurso.CONTENIDO)));

        service.publicar(21L, TipoRecurso.CONTENIDO);

        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_PUBLICADO), eq("Recurso"), eq(21L), any());
    }

    @Test
    void archivarRegistraRecursoArchivado() {
        when(recursoRepo.findById(3L)).thenReturn(Optional.of(recurso(3, TipoRecurso.RUTA)));
        service.archivar(3L, TipoRecurso.RUTA);
        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_ARCHIVADO), eq("Recurso"), eq(3L), any());
    }

    @Test
    void eliminarRegistraRecursoEliminado() {
        when(recursoRepo.findById(4L)).thenReturn(Optional.of(recurso(4, TipoRecurso.RUTA)));
        when(recursoRepo.estaAsignadoComoRutaDeNivel(4L)).thenReturn(false);

        service.eliminar(4L, TipoRecurso.RUTA);

        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_ELIMINADO), eq("Recurso"), eq(4L), any());
    }

    // Ajuste Laura #3: no se elimina una ruta asignada como ruta aplicable de un nivel.
    @Test
    void eliminarRutaEnUsoLanzaOperacionNoPermitidaYNoBorraNada() {
        when(recursoRepo.findById(22L)).thenReturn(Optional.of(recurso(22, TipoRecurso.RUTA)));
        when(recursoRepo.estaAsignadoComoRutaDeNivel(22L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(22L, TipoRecurso.RUTA))
                .isInstanceOf(OperacionNoPermitida.class);

        verify(recursoRepo, never()).delete(any());
        verify(bitacora, never()).registrar(eq(AccionBitacora.RECURSO_ELIMINADO), any(), any(), any());
    }
}
