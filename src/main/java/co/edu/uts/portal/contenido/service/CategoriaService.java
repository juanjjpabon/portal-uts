package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.common.Slugs;
import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.web.dto.CategoriaForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de categorias (HU-02, HU-18). Las mutaciones exigen ADMIN_FUNCIONAL (HU-17,
 * segunda capa sobre la regla de URL).
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByOrdenAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> activas() {
        return categoriaRepository.findByActivaTrueOrderByOrdenAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Categoria " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public long contarRecursos(Long categoriaId) {
        return categoriaRepository.contarRecursos(categoriaId);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public Categoria crear(CategoriaForm form) {
        Categoria c = new Categoria(form.getNombre().trim(), slugUnico(form.getNombre(), null));
        aplicar(c, form);
        return categoriaRepository.save(c);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizar(Long id, CategoriaForm form) {
        Categoria c = obtener(id);
        if (!c.getNombre().equalsIgnoreCase(form.getNombre().trim())) {
            c.setSlug(slugUnico(form.getNombre(), id));
        }
        c.setNombre(form.getNombre().trim());
        aplicar(c, form);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void eliminar(Long id) {
        Categoria c = obtener(id);
        long enUso = categoriaRepository.contarRecursos(id);
        if (enUso > 0) {
            throw new OperacionNoPermitida(
                    "No se puede eliminar: la categoria tiene " + enUso + " recurso(s) asociado(s).");
        }
        categoriaRepository.delete(c);
    }

    private void aplicar(Categoria c, CategoriaForm form) {
        c.setDescripcion(form.getDescripcion());
        c.setActiva(form.isActiva());
        c.setOrden(form.getOrden());
    }

    private String slugUnico(String nombre, Long idActual) {
        String base = Slugs.de(nombre);
        String slug = base;
        int n = 2;
        while (existeOtroConSlug(slug, idActual)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    private boolean existeOtroConSlug(String slug, Long idActual) {
        return categoriaRepository.findBySlug(slug)
                .filter(c -> !c.getId().equals(idActual))
                .isPresent();
    }
}
