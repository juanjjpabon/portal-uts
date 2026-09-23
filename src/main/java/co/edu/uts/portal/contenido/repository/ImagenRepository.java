package co.edu.uts.portal.contenido.repository;

import co.edu.uts.portal.contenido.domain.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ImagenRepository extends JpaRepository<Imagen, UUID> {

    /**
     * Borra las imagenes que ya no usa ningun recurso ni ningun paso (p. ej. al
     * reemplazar la imagen de un contenido o al eliminar una ruta). Asi la base de
     * datos no acumula archivos huerfanos.
     */
    @Modifying
    @Query("""
            delete from Imagen i
            where not exists (select 1 from Recurso r where r.imagenId = i.id)
              and not exists (select 1 from PasoRuta p where p.imagenId = i.id)
            """)
    int eliminarHuerfanas();
}
