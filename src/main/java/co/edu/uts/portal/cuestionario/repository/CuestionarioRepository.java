package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    List<Cuestionario> findAllByOrderByNombreAsc();
}
