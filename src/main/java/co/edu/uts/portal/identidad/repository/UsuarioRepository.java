package co.edu.uts.portal.identidad.repository;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByNombreCompletoAsc();

    @Query("""
            select count(u) from Usuario u join u.roles r
            where r.nombre = :rol and u.activo = true
            """)
    long contarActivosConRol(@Param("rol") NombreRol rol);

    /** Panel admin (ajuste Laura #6). */
    long countByActivoTrue();
}
