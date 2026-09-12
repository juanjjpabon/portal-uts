package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.service.ValoracionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-25: la valoracion solo alimenta el contador agregado del propio Recurso. */
class ValoracionServiceTest {

    private final RecursoRepository repo = mock(RecursoRepository.class);
    private final ValoracionService service = new ValoracionService(repo);

    private Recurso recurso(EstadoPublicacion estado) {
        Recurso r = new Recurso(TipoRecurso.CONTENIDO);
        ReflectionTestUtils.setField(r, "id", 1L);
        ReflectionTestUtils.setField(r, "estado", estado);
        return r;
    }

    @Test
    void votoUtilEnRecursoPublicadoIncrementaElContadorUtil() {
        when(repo.findById(1L)).thenReturn(Optional.of(recurso(EstadoPublicacion.PUBLICADO)));
        service.registrar(1L, true);
        verify(repo).incrementarValoracionUtil(1L);
        verify(repo, never()).incrementarValoracionNoUtil(1L);
    }

    @Test
    void votoNoUtilIncrementaElContadorContrario() {
        when(repo.findById(2L)).thenReturn(Optional.of(recurso(EstadoPublicacion.PUBLICADO)));
        service.registrar(2L, false);
        verify(repo).incrementarValoracionNoUtil(2L);
        verify(repo, never()).incrementarValoracionUtil(2L);
    }

    @Test
    void ignoraElVotoSiElRecursoNoEstaPublicado() {
        when(repo.findById(3L)).thenReturn(Optional.of(recurso(EstadoPublicacion.BORRADOR)));
        service.registrar(3L, true);
        verify(repo, never()).incrementarValoracionUtil(3L);
    }

    @Test
    void ignoraElVotoSiElRecursoNoExiste() {
        when(repo.findById(4L)).thenReturn(Optional.empty());
        service.registrar(4L, true);
        verify(repo, never()).incrementarValoracionUtil(4L);
        verify(repo, never()).incrementarValoracionNoUtil(4L);
    }
}
