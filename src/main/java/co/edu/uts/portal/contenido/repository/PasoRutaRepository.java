package co.edu.uts.portal.contenido.repository;

import co.edu.uts.portal.contenido.domain.PasoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PasoRutaRepository extends JpaRepository<PasoRuta, Long> {

    List<PasoRuta> findByRutaIdOrderByOrdenAscIdAsc(Long rutaId);

    /** Pasos de varias rutas en una sola consulta (pagina publica de rutas). */
    List<PasoRuta> findByRutaIdInOrderByRutaIdAscOrdenAscIdAsc(Collection<Long> rutaIds);

    /** Cantidad de pasos por ruta, para el listado del panel: filas [rutaId, cantidad]. */
    @Query("select p.rutaId, count(p) from PasoRuta p where p.rutaId in :rutaIds group by p.rutaId")
    List<Object[]> contarPorRuta(@Param("rutaIds") Collection<Long> rutaIds);
}
