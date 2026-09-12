package co.edu.uts.portal.contenido.service;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.repository.RecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-25: valoracion de utilidad de un contenido o ruta. Publica, opcional, sin dato
 * personal -- ni siquiera se evita el doble voto, porque eso exigiria identificar al
 * visitante. Solo alimenta el contador agregado del propio Recurso.
 */
@Service
public class ValoracionService {

    private final RecursoRepository recursoRepository;

    public ValoracionService(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Transactional
    public void registrar(Long recursoId, boolean util) {
        boolean publicado = recursoRepository.findById(recursoId)
                .map(r -> r.getEstado() == EstadoPublicacion.PUBLICADO)
                .orElse(false);
        if (!publicado) {
            return;   // recurso inexistente/no publicado: se ignora en silencio, sin error ruidoso
        }
        if (util) {
            recursoRepository.incrementarValoracionUtil(recursoId);
        } else {
            recursoRepository.incrementarValoracionNoUtil(recursoId);
        }
    }
}
