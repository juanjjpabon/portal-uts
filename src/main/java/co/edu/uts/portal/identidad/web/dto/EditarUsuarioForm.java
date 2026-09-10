package co.edu.uts.portal.identidad.web.dto;

import co.edu.uts.portal.identidad.domain.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditarUsuarioForm {

    @NotBlank
    @Size(max = 120)
    private String nombreCompleto;

    @NotBlank
    @Email
    @Size(max = 160)
    private String correo;

    public static EditarUsuarioForm de(Usuario u) {
        EditarUsuarioForm f = new EditarUsuarioForm();
        f.nombreCompleto = u.getNombreCompleto();
        f.correo = u.getCorreo();
        return f;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
