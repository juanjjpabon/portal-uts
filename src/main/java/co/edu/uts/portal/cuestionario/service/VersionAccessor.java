package co.edu.uts.portal.cuestionario.service;

import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.repository.CuestionarioRepository;
import org.springframework.stereotype.Component;

/**
 * Carga una version dentro de la transaccion actual y verifica que sea editable
 * (BORRADOR). Punto unico de esa comprobacion para los servicios de preguntas y
 * niveles (HU-20: una version en uso no se altera).
 */
@Component
public class VersionAccessor {

    private final CuestionarioRepository cuestionarioRepository;

    public VersionAccessor(CuestionarioRepository cuestionarioRepository) {
        this.cuestionarioRepository = cuestionarioRepository;
    }

    public CuestionarioVersion editable(Long cuestionarioId, int numero) {
        CuestionarioVersion v = cuestionarioRepository.findById(cuestionarioId)
                .flatMap(c -> c.version(numero))
                .orElseThrow(() -> new CuestionarioNoEncontrado(
                        "Cuestionario " + cuestionarioId + " version " + numero + " no existe"));
        if (!v.esEditable()) {
            throw new VersionNoEditable("La versión " + numero + " está "
                    + v.getEstado().getEtiqueta().toLowerCase() + " y no se puede modificar.");
        }
        return v;
    }
}
