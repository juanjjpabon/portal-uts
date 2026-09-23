package co.edu.uts.portal.contenido;

import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-04 / HU-05: la validacion por tipo del recurso generico (F-DC-125).
 */
class RecursoFormValidacionTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private Set<String> camposConError(RecursoForm form) {
        return validator.validate(form).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void contenidoExigeResumenCuerpoYCategoria() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.CONTENIDO);
        f.setTitulo("Senales de alerta");

        assertThat(camposConError(f)).contains("resumen", "cuerpo", "categoriaIds");
    }

    @Test
    void contenidoValidoNoTieneErrores() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.CONTENIDO);
        f.setTitulo("Senales de alerta");
        f.setResumen("Resumen breve");
        f.setCuerpo("Cuerpo del contenido");
        f.setCategoriaIds(Set.of(1L));

        assertThat(camposConError(f)).isEmpty();
    }

    @Test
    void rutaExigeDependenciaHorarioYCanal() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.RUTA);
        f.setTitulo("Bienestar universitario");

        assertThat(camposConError(f)).contains("dependencia", "horario", "canal");
    }

    @Test
    void rutaNoExigeResumenNiCuerpo() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.RUTA);
        f.setTitulo("Bienestar universitario");
        f.setDependencia("Bienestar");
        f.setHorario("L-V 8-17");
        f.setCanal("Linea 123");

        assertThat(camposConError(f)).isEmpty();
    }

    @Test
    void contactoNoExigeHorario() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.CONTACTO);
        f.setTitulo("Psicologo de turno");
        f.setDependencia("Bienestar");
        f.setCanal("psicologia@uts.edu.co");

        assertThat(camposConError(f)).isEmpty();
    }

    @Test
    void contactoDirectoDebeSerCorreoTelefonoOEnlace() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.RUTA);
        f.setTitulo("Tutorías");
        f.setDependencia("CAE");
        f.setHorario("L-V");
        f.setCanal("Correo del CAE");

        f.setUrlCanal("javascript:alert(1)");
        assertThat(camposConError(f)).contains("urlCanal");

        f.setUrlCanal("cae@correo.uts.edu.co");
        assertThat(camposConError(f)).isEmpty();

        f.setUrlCanal("");
        assertThat(camposConError(f)).isEmpty();
    }

    @Test
    void contactoDirectoDemasiadoLargoTrasNormalizarSeRechaza() {
        RecursoForm f = RecursoForm.nuevo(TipoRecurso.CONTACTO);
        f.setTitulo("Sitio");
        f.setDependencia("CAE");
        f.setCanal("Web");
        // 300 caracteres sin esquema: al guardarse gana "https://" y ya no cabe en la columna.
        f.setUrlCanal("www.uts.edu.co/" + "a".repeat(300 - "www.uts.edu.co/".length()));

        assertThat(camposConError(f)).contains("urlCanal");
    }
}
