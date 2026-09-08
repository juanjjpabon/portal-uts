package co.edu.uts.portal.contenido.domain.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que un {@link RecursoValidable} traiga los campos que exige su tipo
 * (HU-04 para CONTENIDO, HU-05 para RUTA/CONTACTO). Se aplica al DTO del formulario.
 */
@Documented
@Constraint(validatedBy = RecursoCoherenteValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecursoCoherente {

    String message() default "Faltan datos obligatorios para este tipo de recurso";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
