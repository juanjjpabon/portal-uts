package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.domain.PasoRuta;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.repository.PasoRutaRepository;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import co.edu.uts.portal.contenido.web.dto.PasoRutaForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Pasos de una ruta institucional (reunion con la directora, 22/9/2026): la ruta
 * explica que hacer y en que orden, no solo a quien contactar.
 *
 * Lectura abierta al panel; cambios solo para ADMIN_FUNCIONAL (HU-17), cada uno en
 * la bitacora (HU-22). El orden siempre queda consecutivo (1, 2, 3...), aunque se
 * borre o se mueva un paso.
 */
@Service
public class PasoRutaService {

    private static final String OBJ = "Ruta";

    private final PasoRutaRepository pasoRutaRepository;
    private final RecursoRepository recursoRepository;
    private final ImagenService imagenService;
    private final BitacoraService bitacora;

    public PasoRutaService(PasoRutaRepository pasoRutaRepository, RecursoRepository recursoRepository,
                           ImagenService imagenService, BitacoraService bitacora) {
        this.pasoRutaRepository = pasoRutaRepository;
        this.recursoRepository = recursoRepository;
        this.imagenService = imagenService;
        this.bitacora = bitacora;
    }

    /** La ruta a la que pertenecen los pasos; 404 si no existe o no es una ruta. */
    @Transactional(readOnly = true)
    public Recurso ruta(Long rutaId) {
        return recursoRepository.findById(rutaId)
                .filter(r -> r.getTipo() == TipoRecurso.RUTA)
                .orElseThrow(() -> new RecursoNoEncontrado("La ruta " + rutaId + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<PasoRuta> listar(Long rutaId) {
        return pasoRutaRepository.findByRutaIdOrderByOrdenAscIdAsc(rutaId);
    }

    @Transactional(readOnly = true)
    public PasoRuta obtener(Long rutaId, Long pasoId) {
        return pasoRutaRepository.findById(pasoId)
                .filter(p -> p.getRutaId().equals(rutaId))
                .orElseThrow(() -> new RecursoNoEncontrado("El paso " + pasoId + " no existe en la ruta " + rutaId));
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public PasoRuta crear(Long rutaId, PasoRutaForm form, ImagenProcesada imagenNueva) {
        Recurso ruta = ruta(rutaId);
        int siguiente = listar(rutaId).size() + 1;
        PasoRuta paso = new PasoRuta(rutaId, siguiente);
        aplicar(paso, form);
        if (imagenNueva != null) {
            paso.setImagenId(imagenService.guardar(imagenNueva).getId());
        }
        pasoRutaRepository.save(paso);
        bitacora.registrar(AccionBitacora.PASO_RUTA_CREADO, OBJ, rutaId,
                "Agregó el paso " + siguiente + " \"" + paso.getTitulo() + "\" a la ruta \"" + ruta.getTitulo() + "\"");
        return paso;
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void actualizar(Long rutaId, Long pasoId, PasoRutaForm form, ImagenProcesada imagenNueva) {
        Recurso ruta = ruta(rutaId);
        PasoRuta paso = obtener(rutaId, pasoId);
        aplicar(paso, form);
        boolean cambioImagen = false;
        if (imagenNueva != null) {
            paso.setImagenId(imagenService.guardar(imagenNueva).getId());
            cambioImagen = true;
        } else if (form.isQuitarImagen() && paso.getImagenId() != null) {
            paso.setImagenId(null);
            cambioImagen = true;
        }
        bitacora.registrar(AccionBitacora.PASO_RUTA_ACTUALIZADO, OBJ, rutaId,
                "Actualizó el paso " + paso.getOrden() + " \"" + paso.getTitulo() + "\" de la ruta \""
                        + ruta.getTitulo() + "\"");
        if (cambioImagen) {
            pasoRutaRepository.flush();
            imagenService.eliminarHuerfanas();
        }
    }

    /**
     * Sube (desplazamiento -1) o baja (+1) un paso, intercambiandolo con su vecino.
     * En los extremos no hace nada.
     */
    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void mover(Long rutaId, Long pasoId, int desplazamiento) {
        Recurso ruta = ruta(rutaId);
        List<PasoRuta> pasos = new java.util.ArrayList<>(listar(rutaId));
        int i = indiceDe(pasos, pasoId);
        int j = i + Integer.signum(desplazamiento);
        if (j < 0 || j >= pasos.size()) {
            return;
        }
        java.util.Collections.swap(pasos, i, j);
        renumerar(pasos);
        bitacora.registrar(AccionBitacora.PASO_RUTA_REORDENADO, OBJ, rutaId,
                "Movió el paso \"" + pasos.get(j).getTitulo() + "\" a la posición " + (j + 1)
                        + " en la ruta \"" + ruta.getTitulo() + "\"");
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public void eliminar(Long rutaId, Long pasoId) {
        Recurso ruta = ruta(rutaId);
        PasoRuta paso = obtener(rutaId, pasoId);
        pasoRutaRepository.delete(paso);
        List<PasoRuta> restantes = new java.util.ArrayList<>(listar(rutaId));
        restantes.removeIf(p -> p.getId().equals(pasoId));
        renumerar(restantes);
        bitacora.registrar(AccionBitacora.PASO_RUTA_ELIMINADO, OBJ, rutaId,
                "Eliminó el paso \"" + paso.getTitulo() + "\" de la ruta \"" + ruta.getTitulo() + "\"");
        if (paso.getImagenId() != null) {
            pasoRutaRepository.flush();
            imagenService.eliminarHuerfanas();
        }
    }

    private static void aplicar(PasoRuta paso, PasoRutaForm form) {
        paso.setTitulo(form.getTitulo().trim());
        paso.setDescripcion(StringUtils.hasText(form.getDescripcion()) ? form.getDescripcion().trim() : null);
        paso.setImagenAlt(StringUtils.hasText(form.getImagenAlt()) ? form.getImagenAlt().trim() : null);
    }

    private static int indiceDe(List<PasoRuta> pasos, Long pasoId) {
        for (int i = 0; i < pasos.size(); i++) {
            if (pasos.get(i).getId().equals(pasoId)) {
                return i;
            }
        }
        throw new RecursoNoEncontrado("El paso " + pasoId + " no existe en esta ruta");
    }

    private static void renumerar(List<PasoRuta> pasos) {
        for (int i = 0; i < pasos.size(); i++) {
            pasos.get(i).setOrden(i + 1);
        }
    }
}
