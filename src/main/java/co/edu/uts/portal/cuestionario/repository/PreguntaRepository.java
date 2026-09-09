package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
}
