package co.edu.uts.portal.contenido.domain.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * Exige los campos obligatorios segun el tipo:
 *  - CONTENIDO (HU-04): resumen, cuerpo y al menos una categoria (HU-02).
 *  - RUTA (HU-05): dependencia, horario y canal.
 *  - CONTACTO: dependencia y canal.
 * Cada error se asocia a su campo para que Thymeleaf lo muestre al lado.
 */
public class RecursoCoherenteValidator implements ConstraintValidator<RecursoCoherente, RecursoValidable> {

    @Override
    public boolean isValid(RecursoValidable r, ConstraintValidatorContext ctx) {
        if (r == null || r.getTipo() == null) {
            return true; // otras validaciones (@NotNull) se encargan
        }
        ctx.disableDefaultConstraintViolation();
        boolean ok = true;

        switch (r.getTipo()) {
            case CONTENIDO -> {
                ok &= exigir(ctx, r.getResumen(), "resumen", "El resumen es obligatorio para un contenido");
                ok &= exigir(ctx, r.getCuerpo(), "cuerpo", "El cuerpo es obligatorio para un contenido");
                if (!r.tieneCategorias()) {
                    violacion(ctx, "categoriaIds", "Selecciona al menos una categoria");
                    ok = false;
                }
            }
            case RUTA -> {
                ok &= exigir(ctx, r.getDependencia(), "dependencia", "La dependencia es obligatoria para una ruta");
                ok &= exigir(ctx, r.getHorario(), "horario", "El horario es obligatorio para una ruta");
                ok &= exigir(ctx, r.getCanal(), "canal", "El canal es obligatorio para una ruta");
            }
            case CONTACTO -> {
                ok &= exigir(ctx, r.getDependencia(), "dependencia", "La dependencia es obligatoria para un contacto");
                ok &= exigir(ctx, r.getCanal(), "canal", "El canal es obligatorio para un contacto");
            }
        }
        return ok;
    }

    private boolean exigir(ConstraintValidatorContext ctx, String valor, String campo, String mensaje) {
        if (StringUtils.hasText(valor)) {
            return true;
        }
        violacion(ctx, campo, mensaje);
        return false;
    }

    private void violacion(ConstraintValidatorContext ctx, String campo, String mensaje) {
        ctx.buildConstraintViolationWithTemplate(mensaje)
                .addPropertyNode(campo)
                .addConstraintViolation();
    }
}
