package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.contenido.domain.Categoria;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.CategoriaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Capa de solo lectura para el portal publico (HU-01 a HU-05). Nunca expone un
 * Recurso que no este en estado PUBLICADO, sin importar como se pida (listado,
 * slug directo o busqueda).
 */
@Service
public class PortalPublicoService {

    private final RecursoRepository recursoRepository;
    private final CategoriaRepository categoriaRepository;

    public PortalPublicoService(RecursoRepository recursoRepository, CategoriaRepository categoriaRepository) {
        this.recursoRepository = recursoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> categorias() {
        return categoriaRepository.findByActivaTrueOrderByOrdenAscNombreAsc();
    }

    /** HU-01/HU-02: contenidos publicados, opcionalmente filtrados por categoria. */
    @Transactional(readOnly = true)
    public List<Recurso> contenidos(String categoriaSlug) {
        if (StringUtils.hasText(categoriaSlug)) {
            return recursoRepository.buscarPorCategoria(
                    TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO, categoriaSlug);
        }
        return recursoRepository.findByTipoAndEstadoOrderByOrdenAscTituloAsc(
                TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO);
    }

    /** Ultimos contenidos publicados, para la vitrina de la portada (HU-01). */
    @Transactional(readOnly = true)
    public List<Recurso> ultimosContenidos(int cantidad) {
        return recursoRepository.findByTipoAndEstadoOrderByOrdenAscTituloAsc(
                        TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO)
                .stream().limit(cantidad).toList();
    }

    /** HU-04: detalle de un contenido publicado. Vacio si no existe o no esta publicado. */
    @Transactional(readOnly = true)
    public Recurso contenido(String slug) {
        return recursoRepository.findBySlugAndEstado(slug, EstadoPublicacion.PUBLICADO)
                .filter(r -> r.getTipo() == TipoRecurso.CONTENIDO)
                .orElseThrow(() -> new ContenidoNoDisponible("Contenido no disponible: " + slug));
    }

    /** HU-23: cuenta una vista. Contador agregado en el propio Recurso, sin registrar quien vio que. */
    @Transactional
    public void registrarVista(Long recursoId) {
        recursoRepository.incrementarVistas(recursoId);
    }

    /** HU-05: rutas institucionales publicadas. */
    @Transactional(readOnly = true)
    public List<Recurso> rutas() {
        return recursoRepository.findByTipoAndEstadoOrderByOrdenAscTituloAsc(
                TipoRecurso.RUTA, EstadoPublicacion.PUBLICADO);
    }

    /** Contactos publicados, mostrados junto a las rutas en la misma pagina. */
    @Transactional(readOnly = true)
    public List<Recurso> contactos() {
        return recursoRepository.findByTipoAndEstadoOrderByOrdenAscTituloAsc(
                TipoRecurso.CONTACTO, EstadoPublicacion.PUBLICADO);
    }

    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}");
    private static final Pattern PUNTUACION = Pattern.compile("[,.;:!?'\"()]");
    private static final Pattern ESPACIOS = Pattern.compile("\\s+");

    /**
     * HU-03: busqueda publica. q en blanco -> lista vacia (el controlador no la ejecuta).
     * Compara texto normalizado (sin tildes/enie, en minuscula, sin puntuacion) para que
     * no haga falta escribirlo exactamente como esta guardado (hallazgo de usabilidad,
     * 21/9/2026: pidieron que la tilde o la coma no fueran obligatorias para buscar).
     */
    @Transactional(readOnly = true)
    public List<Recurso> buscar(String q) {
        String qNormalizado = normalizar(q);
        if (qNormalizado.isEmpty()) {
            return List.of();
        }
        return recursoRepository.findByEstadoOrderByTipoAscOrdenAscTituloAsc(EstadoPublicacion.PUBLICADO)
                .stream()
                .filter(r -> coincide(r, qNormalizado))
                .toList();
    }

    private static boolean coincide(Recurso r, String qNormalizado) {
        String haystack = r.getTipo() == TipoRecurso.CONTENIDO
                ? normalizar(r.getTitulo()) + " " + normalizar(r.getResumen()) + " " + normalizar(r.getCuerpo())
                : normalizar(r.getTitulo()) + " " + normalizar(r.getDependencia()) + " " + normalizar(r.getCanal());
        return haystack.contains(qNormalizado);
    }

    /** Sin tildes/enie, en minuscula y sin puntuacion, para comparar de forma tolerante. */
    private static String normalizar(String texto) {
        if (!StringUtils.hasText(texto)) {
            return "";
        }
        String sinAcentos = DIACRITICOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        String sinPuntuacion = PUNTUACION.matcher(sinAcentos.toLowerCase()).replaceAll(" ");
        return ESPACIOS.matcher(sinPuntuacion).replaceAll(" ").trim();
    }
}
