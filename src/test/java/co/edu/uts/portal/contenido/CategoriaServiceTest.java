package co.edu.uts.portal.contenido;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.service.CategoriaService;
import co.edu.uts.portal.contenido.web.dto.CategoriaForm;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-22: cada mutacion de Categoria queda en la bitacora. */
class CategoriaServiceTest {

    private final CategoriaRepository repo = mock(CategoriaRepository.class);
    private final BitacoraService bitacora = mock(BitacoraService.class);
    private final CategoriaService service = new CategoriaService(repo, bitacora);

    private Categoria categoria(long id, String nombre) {
        Categoria c = new Categoria(nombre, nombre.toLowerCase());
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private CategoriaForm form(String nombre) {
        CategoriaForm f = CategoriaForm.nueva();
        f.setNombre(nombre);
        f.setActiva(true);
        return f;
    }

    @Test
    void crearRegistraCategoriaCreada() {
        when(repo.findBySlug(any())).thenReturn(Optional.empty());
        service.crear(form("Ansiedad"));
        verify(bitacora).registrar(eq(AccionBitacora.CATEGORIA_CREADA), eq("Categoria"), any(), any());
    }

    @Test
    void actualizarRegistraCategoriaActualizada() {
        when(repo.findById(1L)).thenReturn(Optional.of(categoria(1, "Ansiedad")));
        when(repo.findBySlug(any())).thenReturn(Optional.empty());
        service.actualizar(1L, form("Ansiedad"));
        verify(bitacora).registrar(eq(AccionBitacora.CATEGORIA_ACTUALIZADA), eq("Categoria"), eq(1L), any());
    }

    @Test
    void eliminarSinUsoRegistraCategoriaEliminada() {
        when(repo.findById(2L)).thenReturn(Optional.of(categoria(2, "Estres")));
        when(repo.contarRecursos(2L)).thenReturn(0L);
        service.eliminar(2L);
        verify(bitacora).registrar(eq(AccionBitacora.CATEGORIA_ELIMINADA), eq("Categoria"), eq(2L), any());
    }
}
