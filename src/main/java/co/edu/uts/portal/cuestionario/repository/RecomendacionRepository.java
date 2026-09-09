package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.Recomendacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {
}
