package co.edu.uts.portal.contenido.repository;

import co.edu.uts.portal.contenido.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByOrdenAscNombreAsc();

    List<Categoria> findByActivaTrueOrderByOrdenAscNombreAsc();

    Optional<Categoria> findBySlug(String slug);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsBySlug(String slug);

    @Query("select count(r) from Recurso r join r.categorias c where c.id = :categoriaId")
    long contarRecursos(Long categoriaId);
}
