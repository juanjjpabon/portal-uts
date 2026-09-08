package co.edu.uts.portal.identidad.domain;

import co.edu.uts.portal.common.domain.BaseAuditable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Usuario administrativo del portal (HU-16, HU-17, HU-21).
 *
 * - El identificador de inicio de sesion es {@link #correo}.
 * - {@link #hashContrasena} guarda el hash BCrypt; nunca la contrasena en claro.
 * - {@link #activo} en false deshabilita el acceso sin borrar la cuenta.
 * - Relacion N:M con {@link Rol}: un usuario puede tener uno o ambos roles.
 */
@Entity
@Table(name = "usuario")
public class Usuario extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, unique = true, length = 160)
    private String correo;

    @Column(name = "hash_contrasena", nullable = false, length = 100)
    private String hashContrasena;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String nombreCompleto, String correo, String hashContrasena) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
    }

    public void agregarRol(Rol rol) {
        this.roles.add(rol);
    }

    public void quitarRol(Rol rol) {
        this.roles.remove(rol);
    }

    public void registrarAcceso(Instant momento) {
        this.ultimoAcceso = momento;
    }

    public Long getId() {
        return id;
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

    public String getHashContrasena() {
        return hashContrasena;
    }

    public void setHashContrasena(String hashContrasena) {
        this.hashContrasena = hashContrasena;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getUltimoAcceso() {
        return ultimoAcceso;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }
}
