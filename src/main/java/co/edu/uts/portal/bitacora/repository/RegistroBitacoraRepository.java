package co.edu.uts.portal.bitacora.repository;

import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroBitacoraRepository extends JpaRepository<RegistroBitacora, Long> {

    List<RegistroBitacora> findByTipoObjetoAndObjetoIdOrderByOcurridoEnDesc(String tipoObjeto, String objetoId);

    List<RegistroBitacora> findByOrderByOcurridoEnDesc(Pageable pageable);
}
