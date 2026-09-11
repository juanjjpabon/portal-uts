package co.edu.uts.portal.bitacora.repository;

import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * El filtro combinado de HU-22 (tipoObjeto/desde/hasta, todos opcionales) se resuelve
 * con Specification (ver BitacoraService.buscar) en lugar de un @Query con "is null":
 * un parametro de fecha nulo que solo aparece detras de "is null" hace que Postgres no
 * pueda inferirle un tipo (el mismo problema de fondo que el "lower(bytea)" de HU-18,
 * aqui como "no se pudo determinar el tipo del parametro" / "no se puede convertir
 * bytea a timestamp"). Con Specification, un filtro ausente simplemente no agrega
 * predicado ni parametro: no hay nada que Postgres deba tipar.
 */
public interface RegistroBitacoraRepository
        extends JpaRepository<RegistroBitacora, Long>, JpaSpecificationExecutor<RegistroBitacora> {

    List<RegistroBitacora> findByTipoObjetoAndObjetoIdOrderByOcurridoEnDesc(String tipoObjeto, String objetoId);

    List<RegistroBitacora> findByOrderByOcurridoEnDesc(Pageable pageable);

    @Query("select distinct r.tipoObjeto from RegistroBitacora r order by r.tipoObjeto")
    List<String> findDistinctTipoObjeto();
}
