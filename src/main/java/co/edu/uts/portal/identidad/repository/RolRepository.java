package co.edu.uts.portal.identidad.repository;

import co.edu.uts.portal.identidad.domain.NombreRol;
import co.edu.uts.portal.identidad.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);
}
