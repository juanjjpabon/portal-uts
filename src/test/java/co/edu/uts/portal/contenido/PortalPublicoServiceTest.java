package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.service.ContenidoNoDisponible;
import co.edu.uts.portal.contenido.service.PortalPublicoService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-01/HU-02/HU-04/HU-05: el portal publico solo debe exponer recursos PUBLICADO.
 */
class PortalPublicoServiceTest {

    private final RecursoRepository recursoRepo = mock(RecursoRepository.class);
    private final CategoriaRepository categoriaRepo = mock(CategoriaRepository.class);
    private final PortalPublicoService service = new PortalPublicoService(recursoRepo, categoriaRepo);

    @Test
    void contenidosSinCategoriaUsaElListadoSimple() {
        service.contenidos(null);
        verify(recursoRepo).findByTipoAndEstadoOrderByOrdenAscTituloAsc(
                TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO);
        verify(recursoRepo, never()).buscarPorCategoria(any(), any(), anyString());
    }

    @Test
    void contenidosConCategoriaFiltra() {
        service.contenidos("ansiedad");
        verify(recursoRepo).buscarPorCategoria(TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO, "ansiedad");
    }

    @Test
    void contenidoNoPublicadoNoEstaDisponible() {
        when(recursoRepo.findBySlugAndEstado("x", EstadoPublicacion.PUBLICADO)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.contenido("x")).isInstanceOf(ContenidoNoDisponible.class);
    }

    @Test
    void buscarConTextoVacioNoConsultaLaBaseDeDatos() {
        assertThat(service.buscar("")).isEmpty();
        assertThat(service.buscar(null)).isEmpty();
        verify(recursoRepo, never()).buscarPublico(any(), any(), anyString());
    }

    @Test
    void buscarConTextoDelegaEnElRepositorio() {
        when(recursoRepo.buscarPublico(EstadoPublicacion.PUBLICADO, TipoRecurso.CONTENIDO, "ansiedad"))
                .thenReturn(List.of());
        service.buscar("  ansiedad  ");
        verify(recursoRepo).buscarPublico(EstadoPublicacion.PUBLICADO, TipoRecurso.CONTENIDO, "ansiedad");
    }
}
