package co.edu.uts.portal.contenido.repository;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    Optional<Recurso> findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySlug(String slug);

    /** Portal publico (HU-01/HU-05): un recurso solo es visible si esta PUBLICADO. */
    Optional<Recurso> findBySlugAndEstado(String slug, EstadoPublicacion estado);

    List<Recurso> findByTipoAndEstadoOrderByOrdenAscTituloAsc(TipoRecurso tipo, EstadoPublicacion estado);

    /**
     * Listado del panel (HU-18/HU-19): filtra por tipo y, opcionalmente, por estado,
     * categoria y texto libre en titulo/dependencia.
     */
    @Query("""
            select distinct r from Recurso r
            left join r.categorias c
            where r.tipo = :tipo
              and (:estado is null or r.estado = :estado)
              and (:categoriaId is null or c.id = :categoriaId)
              and (coalesce(:texto, '') = ''
                   or lower(r.titulo) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(r.dependencia, '')) like lower(concat('%', cast(:texto as string), '%')))
            order by r.orden asc, r.titulo asc
            """)
    List<Recurso> buscarParaPanel(@Param("tipo") TipoRecurso tipo,
                                  @Param("estado") EstadoPublicacion estado,
                                  @Param("categoriaId") Long categoriaId,
                                  @Param("texto") String texto);

    /** HU-02: contenidos publicados de una categoria (por su slug). */
    @Query("""
            select distinct r from Recurso r join r.categorias c
            where r.tipo = :tipo and r.estado = :estado and c.slug = :categoriaSlug
            order by r.orden asc, r.titulo asc
            """)
    List<Recurso> buscarPorCategoria(@Param("tipo") TipoRecurso tipo,
                                     @Param("estado") EstadoPublicacion estado,
                                     @Param("categoriaSlug") String categoriaSlug);

    /**
     * HU-03: busqueda publica por palabra clave. En CONTENIDO coincide en
     * titulo/resumen/cuerpo; en RUTA/CONTACTO coincide en titulo/dependencia/canal.
     */
    @Query("""
            select r from Recurso r
            where r.estado = :estado
              and (
                (r.tipo = :contenido and (
                     lower(r.titulo) like lower(concat('%', cast(:q as string), '%'))
                  or lower(coalesce(r.resumen, '')) like lower(concat('%', cast(:q as string), '%'))
                  or lower(coalesce(r.cuerpo, '')) like lower(concat('%', cast(:q as string), '%'))
                ))
                or (r.tipo <> :contenido and (
                     lower(r.titulo) like lower(concat('%', cast(:q as string), '%'))
                  or lower(coalesce(r.dependencia, '')) like lower(concat('%', cast(:q as string), '%'))
                  or lower(coalesce(r.canal, '')) like lower(concat('%', cast(:q as string), '%'))
                ))
              )
            order by r.tipo asc, r.orden asc, r.titulo asc
            """)
    List<Recurso> buscarPublico(@Param("estado") EstadoPublicacion estado,
                                @Param("contenido") TipoRecurso contenido,
                                @Param("q") String q);

    /** HU-23: contador de vista, atomico -- nunca se lee-modifica-escribe la entidad. */
    @Modifying
    @Query("update Recurso r set r.vistas = r.vistas + 1 where r.id = :id")
    void incrementarVistas(@Param("id") Long id);

    /** HU-25: voto de utilidad, atomico y sin ningun dato de quien vota. */
    @Modifying
    @Query("update Recurso r set r.valoracionesUtil = r.valoracionesUtil + 1 where r.id = :id")
    void incrementarValoracionUtil(@Param("id") Long id);

    @Modifying
    @Query("update Recurso r set r.valoracionesNoUtil = r.valoracionesNoUtil + 1 where r.id = :id")
    void incrementarValoracionNoUtil(@Param("id") Long id);

    /** HU-23: contenidos publicados mas consultados, respetando el umbral anti-reidentificacion. */
    @Query("""
            select r from Recurso r
            where r.tipo = :tipo and r.estado = :estado and r.vistas >= :minimo
            order by r.vistas desc
            """)
    List<Recurso> masConsultados(@Param("tipo") TipoRecurso tipo, @Param("estado") EstadoPublicacion estado,
                                 @Param("minimo") int minimo);

    /** HU-23/HU-25: recursos con suficientes valoraciones para mostrarse (umbral anti-reidentificacion). */
    @Query("""
            select r from Recurso r
            where r.estado = :estado and (r.valoracionesUtil + r.valoracionesNoUtil) >= :minimo
            order by (r.valoracionesUtil + r.valoracionesNoUtil) desc
            """)
    List<Recurso> valoradosConSuficientesVotos(@Param("estado") EstadoPublicacion estado,
                                               @Param("minimo") int minimo);
}
