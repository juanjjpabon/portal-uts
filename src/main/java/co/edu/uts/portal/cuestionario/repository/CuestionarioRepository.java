package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.Cuestionario;
import co.edu.uts.portal.cuestionario.domain.EstadoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    List<Cuestionario> findAllByOrderByNombreAsc();

    Optional<Cuestionario> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** Cuestionarios ofrecibles al publico (activos y con version publicada). */
    List<Cuestionario> findByActivoTrueAndVersiones_EstadoOrderByNombreAsc(EstadoVersion estado);

    /** Panel admin (ajuste Laura #6). */
    long countByActivoTrue();

    /** Cuantos cuestionarios tienen (al menos) una version en el estado dado; a lo sumo 1 PUBLICADA por cuestionario. */
    long countByVersiones_Estado(EstadoVersion estado);
}
