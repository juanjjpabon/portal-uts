package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {
}
