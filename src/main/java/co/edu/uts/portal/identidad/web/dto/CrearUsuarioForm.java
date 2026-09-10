package co.edu.uts.portal.identidad.web.dto;

import co.edu.uts.portal.identidad.domain.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

public class CrearUsuarioForm {

    @NotBlank
    @Size(max = 120)
    private String nombreCompleto;

    @NotBlank
    @Email
    @Size(max = 160)
    private String correo;

    @NotBlank
    @Size(min = 8, max = 72, message = "La contrasena inicial debe tener al menos 8 caracteres")
    private String contrasena;

    private Set<NombreRol> roles = new LinkedHashSet<>();

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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Set<NombreRol> getRoles() {
        return roles;
    }

    public void setRoles(Set<NombreRol> roles) {
        this.roles = roles;
    }
}
