package co.edu.uts.portal.cuestionario.repository;

import co.edu.uts.portal.cuestionario.domain.NivelResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NivelResultadoRepository extends JpaRepository<NivelResultado, Long> {

    /** HU-23: contador agregado de resultados de autoorientacion, atomico. */
    @Modifying
    @Query("update NivelResultado n set n.vecesObtenido = n.vecesObtenido + 1 where n.id = :id")
    void incrementarVecesObtenido(@Param("id") Long id);

    /**
     * HU-23: total de veces que se obtuvo cada nivel, sumado entre versiones del
     * mismo cuestionario (una version nueva arranca su contador en 0), filtrando el
     * umbral anti-reidentificacion.
     */
    @Query("""
            select v.cuestionario.nombre as cuestionario, n.nombre as nivel, sum(n.vecesObtenido) as veces
            from NivelResultado n join n.version v
            group by v.cuestionario.nombre, n.nombre
            having sum(n.vecesObtenido) >= :minimo
            order by sum(n.vecesObtenido) desc
            """)
    List<ResumenNivel> resumenPorNivel(@Param("minimo") long minimo);

    interface ResumenNivel {
        String getCuestionario();
        String getNivel();
        long getVeces();
    }
}
