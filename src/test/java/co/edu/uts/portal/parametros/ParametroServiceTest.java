package co.edu.uts.portal.parametros;

import co.edu.uts.portal.parametros.domain.Parametro;
import co.edu.uts.portal.parametros.repository.ParametroRepository;
import co.edu.uts.portal.parametros.service.ParametroService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class ParametroServiceTest {

    private final ParametroRepository repo = mock(ParametroRepository.class);
    private final ParametroService service = new ParametroService(repo);

    private Parametro parametro(long id, String clave, String valor) {
        Parametro p = nuevo();
        setField(p, "id", id);
        setField(p, "clave", clave);
        setField(p, "valor", valor);
        return p;
    }

    private Parametro nuevo() {
        try {
            var ctor = Parametro.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void valorDevuelveElDefaultCuandoNoExisteLaClave() {
        when(repo.findByClave("x")).thenReturn(Optional.empty());
        assertThat(service.valor("x", "fallback")).isEqualTo("fallback");
    }

    @Test
    void valorDevuelveElDefaultCuandoElValorEstaVacio() {
        when(repo.findByClave("x")).thenReturn(Optional.of(parametro(1, "x", "  ")));
        assertThat(service.valor("x", "fallback")).isEqualTo("fallback");
    }

    @Test
    void guardarSoloCuentaLosQueCambian() {
        Parametro a = parametro(1, "a", "viejo");
        Parametro b = parametro(2, "b", "igual");
        when(repo.findAllById(any())).thenReturn(List.of(a, b));

        int cambios = service.guardar(Map.of(1L, "nuevo", 2L, "igual"));

        assertThat(cambios).isEqualTo(1);
        assertThat(a.getValor()).isEqualTo("nuevo");
        assertThat(b.getValor()).isEqualTo("igual");
    }

    @Test
    void guardarConvierteVacioANull() {
        Parametro a = parametro(1, "a", "algo");
        when(repo.findAllById(Set.of(1L))).thenReturn(List.of(a));

        service.guardar(Map.of(1L, "   "));

        assertThat(a.getValor()).isNull();
    }
}
