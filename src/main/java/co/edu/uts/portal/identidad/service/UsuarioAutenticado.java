package co.edu.uts.portal.identidad.service;

import co.edu.uts.portal.identidad.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador entre {@link Usuario} y Spring Security. Expone el id y el correo para
 * que el manejador de exito de login (HU-16) y el AuditorAware (HU-18/HU-22) puedan
 * usarlos sin volver a consultar la base de datos.
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String correo;
    private final String hashContrasena;
    private final boolean activo;
    private final Set<GrantedAuthority> authorities;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.correo = usuario.getCorreo();
        this.hashContrasena = usuario.getHashContrasena();
        this.activo = usuario.isActivo();
        this.authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.authority()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return hashContrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
