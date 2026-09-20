package co.edu.uts.portal.identidad.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de la propia contrasena (HU-21, autoservicio). "actual" no lleva @NotBlank
 * porque en el cambio obligatorio (debeCambiarClave) el formulario ni siquiera la pide
 * -- CuentaService la exige solo cuando corresponde (cambio voluntario).
 */
public class CambiarContrasenaForm {

    private String actual;

    @NotBlank
    @Size(min = 8, max = 72, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String nueva;

    @NotBlank
    private String confirmacion;

    public boolean coincideConfirmacion() {
        return nueva != null && nueva.equals(confirmacion);
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}
