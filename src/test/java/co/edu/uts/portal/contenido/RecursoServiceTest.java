package co.edu.uts.portal.contenido;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-22: cada mutacion de Recurso queda en la bitacora.
 */
class RecursoServiceTest {

    private final RecursoRepository recursoRepo = mock(RecursoRepository.class);
    private final CategoriaRepository categoriaRepo = mock(CategoriaRepository.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final RecursoService service = new RecursoService(recursoRepo, categoriaRepo, bitacora);

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
        service.publicar(2L, TipoRecurso.RUTA);
        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_PUBLICADO), eq("Recurso"), eq(2L), any());
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
        service.eliminar(4L, TipoRecurso.RUTA);
        verify(bitacora).registrar(eq(AccionBitacora.RECURSO_ELIMINADO), eq("Recurso"), eq(4L), any());
    }
}
