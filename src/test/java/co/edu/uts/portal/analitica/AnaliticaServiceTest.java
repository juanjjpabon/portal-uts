package co.edu.uts.portal.analitica;

import co.edu.uts.portal.analitica.service.AnaliticaService;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.cuestionario.repository.NivelResultadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-23/HU-24: el umbral anti-reidentificacion (>=5) lo aplica el repositorio (se
 * verifica aqui que el servicio lo pide con el valor correcto) y el CSV expone
 * exactamente las mismas filas que el reporte, sin agregar nada mas.
 */
class AnaliticaServiceTest {

    private final RecursoRepository recursoRepo = mock(RecursoRepository.class);
    private final NivelResultadoRepository nivelRepo = mock(NivelResultadoRepository.class);
    private final AnaliticaService service = new AnaliticaService(recursoRepo, nivelRepo);

    private Recurso recurso(TipoRecurso tipo, String titulo, int vistas, int util, int noUtil) {
        Recurso r = new Recurso(tipo);
        ReflectionTestUtils.setField(r, "id", 1L);
        r.setTitulo(titulo);
        ReflectionTestUtils.setField(r, "vistas", vistas);
        ReflectionTestUtils.setField(r, "valoracionesUtil", util);
        ReflectionTestUtils.setField(r, "valoracionesNoUtil", noUtil);
        return r;
    }

    private NivelResultadoRepository.ResumenNivel resumen(String cuestionario, String nivel, long veces) {
        return new NivelResultadoRepository.ResumenNivel() {
            public String getCuestionario() {
                return cuestionario;
            }

            public String getNivel() {
                return nivel;
            }

            public long getVeces() {
                return veces;
            }
        };
    }

    @Test
    void pideElUmbralMinimoDeCincoAlRepositorio() {
        when(recursoRepo.masConsultados(TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO, 5))
                .thenReturn(List.of());
        when(nivelRepo.resumenPorNivel(5)).thenReturn(List.of());
        when(recursoRepo.valoradosConSuficientesVotos(EstadoPublicacion.PUBLICADO, 5)).thenReturn(List.of());

        var reporte = service.generarReporte();

        verify(recursoRepo).masConsultados(TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO, 5);
        verify(nivelRepo).resumenPorNivel(5);
        verify(recursoRepo).valoradosConSuficientesVotos(EstadoPublicacion.PUBLICADO, 5);
        assertThat(reporte.estaVacio()).isTrue();
    }

    @Test
    void elReporteReflejaLoQueDevuelveElRepositorio() {
        when(recursoRepo.masConsultados(eq(TipoRecurso.CONTENIDO), eq(EstadoPublicacion.PUBLICADO), eq(5)))
                .thenReturn(List.of(recurso(TipoRecurso.CONTENIDO, "Senales de alerta", 37, 0, 0)));
        when(nivelRepo.resumenPorNivel(5)).thenReturn(List.of(resumen("Tamizaje", "Alto", 18)));
        when(recursoRepo.valoradosConSuficientesVotos(eq(EstadoPublicacion.PUBLICADO), eq(5)))
                .thenReturn(List.of(recurso(TipoRecurso.RUTA, "Bienestar", 0, 22, 3)));

        var reporte = service.generarReporte();

        assertThat(reporte.contenidosMasConsultados()).hasSize(1);
        assertThat(reporte.contenidosMasConsultados().get(0).vistas()).isEqualTo(37);
        assertThat(reporte.autoorientacionPorNivel().get(0).veces()).isEqualTo(18);
        assertThat(reporte.valoraciones().get(0).util()).isEqualTo(22);
        assertThat(reporte.valoraciones().get(0).noUtil()).isEqualTo(3);
    }

    @Test
    void elCsvContieneLasMismasFilasQueElReporteYNadaMas() {
        when(recursoRepo.masConsultados(eq(TipoRecurso.CONTENIDO), eq(EstadoPublicacion.PUBLICADO), eq(5)))
                .thenReturn(List.of(recurso(TipoRecurso.CONTENIDO, "Ansiedad, estres", 10, 0, 0)));
        when(nivelRepo.resumenPorNivel(5)).thenReturn(List.of());
        when(recursoRepo.valoradosConSuficientesVotos(eq(EstadoPublicacion.PUBLICADO), eq(5))).thenReturn(List.of());

        String csv = service.generarCsv();

        assertThat(csv).contains("seccion,nombre,indicador,valor");
        // el titulo trae una coma: debe quedar entre comillas (RFC 4180)
        assertThat(csv).contains("\"Ansiedad, estres\",vistas,10");
        assertThat(csv).doesNotContain("Tamizaje");
    }
}
