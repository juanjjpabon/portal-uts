package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
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
 * segunda capa sobre la regla de URL). Cada cambio queda en la bitacora (HU-22).
 */
@Service
public class CategoriaService {

    private static final String OBJ = "Categoria";

    private final CategoriaRepository categoriaRepository;
    private final BitacoraService bitacora;

    public CategoriaService(CategoriaRepository categoriaRepository, BitacoraService bitacora) {
        this.categoriaRepository = categoriaRepository;
        this.bitacora = bitacora;
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
        categoriaRepository.save(c);
        bitacora.registrar(AccionBitacora.CATEGORIA_CREADA, OBJ, c.getId(), "Creo la categoria \"" + c.getNombre() + "\"");
        return c;
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
        bitacora.registrar(AccionBitacora.CATEGORIA_ACTUALIZADA, OBJ, id,
                "Actualizo la categoria \"" + c.getNombre() + "\"");
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
        bitacora.registrar(AccionBitacora.CATEGORIA_ELIMINADA, OBJ, id, "Elimino la categoria \"" + c.getNombre() + "\"");
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
