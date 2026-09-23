package co.edu.uts.portal.contenido;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.Imagen;
import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.domain.PasoRuta;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.PasoRutaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.service.ImagenService;
import co.edu.uts.portal.contenido.service.PasoRutaService;
import co.edu.uts.portal.contenido.service.RecursoNoEncontrado;
import co.edu.uts.portal.contenido.web.dto.PasoRutaForm;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Rutas paso a paso (reunion con la directora, 22/9/2026): el orden siempre queda
 * consecutivo y cada cambio pasa por la bitacora (HU-22).
 */
class PasoRutaServiceTest {

    private final PasoRutaRepository pasoRepo = mock(PasoRutaRepository.class);
    private final RecursoRepository recursoRepo = mock(RecursoRepository.class);
    private final ImagenService imagenService = mock(ImagenService.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final PasoRutaService service = new PasoRutaService(pasoRepo, recursoRepo, imagenService, bitacora);

    private Recurso recurso(long id, TipoRecurso tipo) {
        Recurso r = new Recurso(tipo);
        ReflectionTestUtils.setField(r, "id", id);
        r.setTitulo("Tutorías");
        return r;
    }

    private PasoRuta paso(long id, long rutaId, int orden, String titulo) {
        PasoRuta p = new PasoRuta(rutaId, orden);
        ReflectionTestUtils.setField(p, "id", id);
        p.setTitulo(titulo);
        return p;
    }

    private PasoRutaForm form(String titulo) {
        PasoRutaForm f = new PasoRutaForm();
        f.setTitulo(titulo);
        f.setDescripcion("  ");
        return f;
    }

    @Test
    void crearPoneElPasoAlFinalYRegistraEnBitacora() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        when(pasoRepo.findByRutaIdOrderByOrdenAscIdAsc(5L))
                .thenReturn(List.of(paso(1, 5, 1, "Escribe al CAE"), paso(2, 5, 2, "Espera la respuesta")));

        PasoRuta nuevo = service.crear(5L, form("  Asiste a la tutoría "), null);

        assertThat(nuevo.getOrden()).isEqualTo(3);
        assertThat(nuevo.getTitulo()).isEqualTo("Asiste a la tutoría");
        assertThat(nuevo.getDescripcion()).isNull();   // en blanco -> null
        verify(pasoRepo).save(nuevo);
        verify(bitacora).registrar(eq(AccionBitacora.PASO_RUTA_CREADO), eq("Ruta"), eq(5L), any());
    }

    @Test
    void crearConImagenGuardaLaImagenYLaAsocia() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        Imagen guardada = mock(Imagen.class);
        UUID id = UUID.randomUUID();
        when(guardada.getId()).thenReturn(id);
        ImagenProcesada procesada = new ImagenProcesada("image/png", new byte[]{1}, 10, 10, "paso.png");
        when(imagenService.guardar(procesada)).thenReturn(guardada);

        PasoRuta nuevo = service.crear(5L, form("Paso con foto"), procesada);

        assertThat(nuevo.getImagenId()).isEqualTo(id);
    }

    @Test
    void soloLasRutasTienenPasos() {
        when(recursoRepo.findById(9L)).thenReturn(Optional.of(recurso(9, TipoRecurso.CONTENIDO)));

        assertThatThrownBy(() -> service.crear(9L, form("x"), null)).isInstanceOf(RecursoNoEncontrado.class);
        verify(pasoRepo, never()).save(any());
    }

    @Test
    void subirUnPasoLoIntercambiaConElAnterior() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        PasoRuta a = paso(1, 5, 1, "A");
        PasoRuta b = paso(2, 5, 2, "B");
        PasoRuta c = paso(3, 5, 3, "C");
        when(pasoRepo.findByRutaIdOrderByOrdenAscIdAsc(5L)).thenReturn(List.of(a, b, c));

        service.mover(5L, 3L, -1);

        assertThat(a.getOrden()).isEqualTo(1);
        assertThat(c.getOrden()).isEqualTo(2);
        assertThat(b.getOrden()).isEqualTo(3);
        verify(bitacora).registrar(eq(AccionBitacora.PASO_RUTA_REORDENADO), eq("Ruta"), eq(5L), any());
    }

    @Test
    void subirElPrimeroNoHaceNada() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        PasoRuta a = paso(1, 5, 1, "A");
        PasoRuta b = paso(2, 5, 2, "B");
        when(pasoRepo.findByRutaIdOrderByOrdenAscIdAsc(5L)).thenReturn(List.of(a, b));

        service.mover(5L, 1L, -1);

        assertThat(a.getOrden()).isEqualTo(1);
        assertThat(b.getOrden()).isEqualTo(2);
        verify(bitacora, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void eliminarRenumeraLosQueQuedanYLimpiaSuImagen() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        PasoRuta a = paso(1, 5, 1, "A");
        PasoRuta b = paso(2, 5, 2, "B");
        b.setImagenId(UUID.randomUUID());
        PasoRuta c = paso(3, 5, 3, "C");
        when(pasoRepo.findById(2L)).thenReturn(Optional.of(b));
        when(pasoRepo.findByRutaIdOrderByOrdenAscIdAsc(5L)).thenReturn(new ArrayList<>(List.of(a, c)));

        service.eliminar(5L, 2L);

        verify(pasoRepo).delete(b);
        assertThat(a.getOrden()).isEqualTo(1);
        assertThat(c.getOrden()).isEqualTo(2);
        verify(imagenService).eliminarHuerfanas();
        verify(bitacora).registrar(eq(AccionBitacora.PASO_RUTA_ELIMINADO), eq("Ruta"), eq(5L), any());
    }

    @Test
    void unPasoDeOtraRutaNoSePuedeEditar() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        when(pasoRepo.findById(7L)).thenReturn(Optional.of(paso(7, 99, 1, "De otra ruta")));

        assertThatThrownBy(() -> service.actualizar(5L, 7L, form("x"), null))
                .isInstanceOf(RecursoNoEncontrado.class);
    }

    @Test
    void quitarLaImagenDeUnPasoLimpiaLasHuerfanas() {
        when(recursoRepo.findById(5L)).thenReturn(Optional.of(recurso(5, TipoRecurso.RUTA)));
        PasoRuta p = paso(4, 5, 1, "Con foto");
        p.setImagenId(UUID.randomUUID());
        when(pasoRepo.findById(4L)).thenReturn(Optional.of(p));
        PasoRutaForm f = form("Con foto");
        f.setQuitarImagen(true);

        service.actualizar(5L, 4L, f, null);

        assertThat(p.getImagenId()).isNull();
        verify(imagenService).eliminarHuerfanas();
        ArgumentCaptor<String> descripcion = ArgumentCaptor.forClass(String.class);
        verify(bitacora).registrar(eq(AccionBitacora.PASO_RUTA_ACTUALIZADO), eq("Ruta"), eq(5L), descripcion.capture());
        assertThat(descripcion.getValue()).contains("Con foto");
    }
}
