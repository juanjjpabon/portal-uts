package co.edu.uts.portal.parametros.repository;

import co.edu.uts.portal.parametros.domain.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParametroRepository extends JpaRepository<Parametro, Long> {

    List<Parametro> findAllByOrderByGrupoAscOrdenAsc();

    Optional<Parametro> findByClave(String clave);
}
