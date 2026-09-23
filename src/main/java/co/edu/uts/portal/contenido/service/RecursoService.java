package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.common.Slugs;
import co.edu.uts.portal.contenido.domain.CanalDirecto;
import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.PasoRutaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CRUD unico para CONTENIDO, RUTA y CONTACTO (F-DC-125, HU-18/HU-19).
 * Lecturas abiertas al panel; mutaciones restringidas a ADMIN_FUNCIONAL (HU-17).
 * El responsable y la fecha de cada cambio los pone el AuditorAware (HU-18).
 */
@Service
public class RecursoService {

    private static final String OBJ = "Recurso";

    private final RecursoRepository recursoRepository;
    private final CategoriaRepository categoriaRepository;
    private final BitacoraService bitacora;
    private final ImagenService imagenService;
    private final PasoRutaRepository pasoRutaRepository;

    public RecursoService(RecursoRepository recursoRepository, CategoriaRepository categoriaRepository,
                          BitacoraService bitacora, ImagenService imagenService,
                          PasoRutaRepository pasoRutaRepository) {
        this.recursoRepository = recursoRepository;
        this.categoriaRepository = categoriaRepository;
        this.bitacora = bitacora;
        this.imagenService = imagenService;
        this.pasoRutaRepository = pasoRutaRepository;
    }

    @Transactional(readOnly = true)
    public long contarPasosDeRuta(Long rutaId) {
        return pasoRutaRepository.findByRutaIdOrderByOrdenAscIdAsc(rutaId).size();
    }

    /** Cantidad de pasos de cada ruta (para el listado del panel). Las que no tienen, no aparecen. */
    @Transactional(readOnly = true)
    public Map<Long, Long> contarPasos(List<Recurso> rutas) {
        Map<Long, Long> conteo = new HashMap<>();
        List<Long> ids = rutas.stream().filter(r -> r.getTipo() == TipoRecurso.RUTA).map(Recurso::getId).toList();
        if (!ids.isEmpty()) {
            for (Object[] fila : pasoRutaRepository.contarPorRuta(ids)) {
                conteo.put((Long) fila[0], (Long) fila[1]);
            }
        }
        return conteo;
    }

    @Transactional(readOnly = true)
    public List<Recurso> listar(TipoRecurso tipo, EstadoPublicacion estado, Long categoriaId, String texto) {
        String t = StringUtils.hasText(texto) ? texto.trim() : null;
        return recursoRepository.buscarParaPanel(tipo, estado, categoriaId, t);
    }

    @Transactional(readOnly = true)
    public Recurso obtener(Long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Recurso " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public Recurso obtenerDeTipo(Long id, TipoRecurso tipo) {
        Recurso r = obtener(id);
        if (r.getTipo() != tipo) {
            throw new RecursoNoEncontrado("El recurso " + id + " no es de tipo " + tipo);
        }
        return r;
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public Recurso crear(RecursoForm form) {
        return crear(form, null);
    }

    /**
     * @param imagenNueva imagen ya procesada (ver ProcesadorImagen), o null si no se subio ninguna.
     */
    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public Recurso crear(RecursoForm form, ImagenProcesada imagenNueva) {
        Recurso r = new Recurso(form.getTipo());
        aplicar(r, form);
        aplicarImagen(r, form, imagenNueva);
        recursoRepository.save(r);
        bitacora.registrar(AccionBitacora.RECURSO_CREADO, OBJ, r.getId(),
                "Creó " + r.getTipo().getEtiquetaSingular().toLowerCase() + " \"" + r.getTitulo() + "\"");
        return r;
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizar(Long id, RecursoForm form) {
        actualizar(id, form, null);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizar(Long id, RecursoForm form, ImagenProcesada imagenNueva) {
        Recurso r = obtenerDeTipo(id, form.getTipo());
        aplicar(r, form);
        boolean cambioImagen = aplicarImagen(r, form, imagenNueva);
        bitacora.registrar(AccionBitacora.RECURSO_ACTUALIZADO, OBJ, id,
                "Actualizó " + r.getTipo().getEtiquetaSingular().toLowerCase() + " \"" + r.getTitulo() + "\""
                        + (cambioImagen ? " (cambió la imagen)" : ""));
        if (cambioImagen) {
            recursoRepository.flush();
            imagenService.eliminarHuerfanas();
        }
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void publicar(Long id, TipoRecurso tipo) {
        Recurso r = obtenerDeTipo(id, tipo);
        r.publicar(Instant.now());
        bitacora.registrar(AccionBitacora.RECURSO_PUBLICADO, OBJ, id, "Publicó \"" + r.getTitulo() + "\"");
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void archivar(Long id, TipoRecurso tipo) {
        Recurso r = obtenerDeTipo(id, tipo);
        r.archivar();
        bitacora.registrar(AccionBitacora.RECURSO_ARCHIVADO, OBJ, id, "Archivó \"" + r.getTitulo() + "\"");
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void eliminar(Long id, TipoRecurso tipo) {
        Recurso r = obtenerDeTipo(id, tipo);
        recursoRepository.delete(r);
        bitacora.registrar(AccionBitacora.RECURSO_ELIMINADO, OBJ, id, "Eliminó \"" + r.getTitulo() + "\"");
        // La base de datos borra los pasos de la ruta (ON DELETE CASCADE); sus imagenes y
        // la del recurso quedan sin uso y se limpian aqui.
        recursoRepository.flush();
        imagenService.eliminarHuerfanas();
    }

    /**
     * Imagen principal y "destacado". Devuelve true si la imagen cambio (nueva o quitada).
     * Destacar sin imagen se permite, pero el carrusel de la portada solo muestra los que tienen.
     */
    private boolean aplicarImagen(Recurso r, RecursoForm form, ImagenProcesada imagenNueva) {
        boolean cambio = false;
        if (imagenNueva != null) {
            r.setImagenId(imagenService.guardar(imagenNueva).getId());
            cambio = true;
        } else if (form.isQuitarImagen() && r.getImagenId() != null) {
            r.setImagenId(null);
            cambio = true;
        }
        r.setImagenAlt(StringUtils.hasText(form.getImagenAlt()) ? form.getImagenAlt().trim() : null);
        r.setDestacado(form.isDestacado());
        return cambio;
    }

    private void aplicar(Recurso r, RecursoForm form) {
        r.setTitulo(form.getTitulo().trim());
        r.setEstado(form.getEstado());
        r.setOrden(form.getOrden());

        if (r.getTipo().esContenido()) {
            r.setResumen(limpio(form.getResumen()));
            r.setCuerpo(limpio(form.getCuerpo()));
            r.setFuente(limpio(form.getFuente()));
            r.setSlug(slugUnico(form.getTitulo(), r.getId()));
            limpiarCamposRuta(r);
        } else {
            r.setDependencia(limpio(form.getDependencia()));
            r.setHorario(limpio(form.getHorario()));
            r.setCanal(limpio(form.getCanal()));
            // Se guarda ya normalizado: "bienestar@..." -> "mailto:bienestar@...", "123" -> "tel:123".
            r.setUrlCanal(CanalDirecto.normalizar(form.getUrlCanal()).orElse(null));
            r.setUrgente(form.isUrgente());
            limpiarCamposContenido(r);
        }

        r.reemplazarCategorias(resolverCategorias(form.getCategoriaIds()));

        if (r.getEstado() == EstadoPublicacion.PUBLICADO && r.getPublicadoEn() == null) {
            r.publicar(Instant.now());
        }
    }

    /**
     * Texto opcional sin espacios sobrantes; vacio se guarda como null. Antes un campo
     * opcional dejado en blanco (p. ej. la fuente) quedaba como "" y el portal mostraba
     * la etiqueta sin valor ("Fuente:").
     */
    private static String limpio(String texto) {
        return StringUtils.hasText(texto) ? texto.trim() : null;
    }

    private Set<Categoria> resolverCategorias(Set<Long> ids) {
        Set<Categoria> resueltas = new LinkedHashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                categoriaRepository.findById(id).ifPresent(resueltas::add);
            }
        }
        return resueltas;
    }

    private void limpiarCamposRuta(Recurso r) {
        r.setDependencia(null);
        r.setHorario(null);
        r.setCanal(null);
        r.setUrlCanal(null);
        r.setUrgente(false);
    }

    private void limpiarCamposContenido(Recurso r) {
        r.setResumen(null);
        r.setCuerpo(null);
        r.setFuente(null);
        r.setSlug(null);
    }

    private String slugUnico(String titulo, Long idActual) {
        String base = Slugs.de(titulo);
        String slug = base;
        int n = 2;
        while (idActual == null
                ? recursoRepository.existsBySlug(slug)
                : recursoRepository.existsBySlugAndIdNot(slug, idActual)) {
            slug = base + "-" + n++;
        }
        return slug;
    }
}
