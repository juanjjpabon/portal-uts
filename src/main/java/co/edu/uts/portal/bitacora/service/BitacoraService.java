package co.edu.uts.portal.bitacora.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import co.edu.uts.portal.bitacora.repository.RegistroBitacoraRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Punto unico para registrar y consultar la bitacora (HU-22). HU-21 la usa para
 * auditar cada cambio de usuarios y roles. El registro va en la misma transaccion
 * que la operacion auditada: si esta falla, no queda rastro de algo que no paso.
 */
@Service
public class BitacoraService {

    private final RegistroBitacoraRepository repository;
    private final AuditorAware<String> auditorAware;

    public BitacoraService(RegistroBitacoraRepository repository, AuditorAware<String> auditorAware) {
        this.repository = repository;
        this.auditorAware = auditorAware;
    }

    @Transactional
    public void registrar(AccionBitacora accion, String tipoObjeto, Object objetoId, String descripcion) {
        String actor = auditorAware.getCurrentAuditor().orElse("sistema");
        String id = objetoId == null ? null : objetoId.toString();
        repository.save(new RegistroBitacora(actor, accion, tipoObjeto, id, descripcion));
    }

    @Transactional(readOnly = true)
    public List<RegistroBitacora> historial(String tipoObjeto, Object objetoId) {
        return repository.findByTipoObjetoAndObjetoIdOrderByOcurridoEnDesc(
                tipoObjeto, objetoId == null ? null : objetoId.toString());
    }

    @Transactional(readOnly = true)
    public List<RegistroBitacora> ultimos(int cantidad) {
        return repository.findByOrderByOcurridoEnDesc(PageRequest.of(0, cantidad));
    }
}
