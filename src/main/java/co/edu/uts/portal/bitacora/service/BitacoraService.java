package co.edu.uts.portal.bitacora.service;

import co.edu.uts.portal.bitacora.domain.AccionBitacora;
import co.edu.uts.portal.bitacora.domain.RegistroBitacora;
import co.edu.uts.portal.bitacora.repository.RegistroBitacoraRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
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

    /**
     * Visor global (HU-22): solo ADMIN_TECNICO, aunque registrar() queda abierto a todos
     * los modulos. Se arma con Specification en vez de un @Query con "is null" para que
     * un filtro ausente no envie ningun parametro (evita el problema de Postgres sin
     * poder inferir el tipo de un Instant nulo que solo se compara contra IS NULL).
     */
    @PreAuthorize("hasRole('ADMIN_TECNICO')")
    @Transactional(readOnly = true)
    public Page<RegistroBitacora> buscar(String tipoObjeto, Instant desde, Instant hasta, Pageable pageable) {
        List<Specification<RegistroBitacora>> condiciones = new ArrayList<>();
        if (tipoObjeto != null) {
            condiciones.add((root, query, cb) -> cb.equal(root.get("tipoObjeto"), tipoObjeto));
        }
        if (desde != null) {
            condiciones.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ocurridoEn"), desde));
        }
        if (hasta != null) {
            condiciones.add((root, query, cb) -> cb.lessThan(root.get("ocurridoEn"), hasta));
        }
        Specification<RegistroBitacora> spec = Specification.allOf(condiciones);
        return repository.findAll(spec, pageable);
    }

    @PreAuthorize("hasRole('ADMIN_TECNICO')")
    @Transactional(readOnly = true)
    public List<String> tiposObjeto() {
        return repository.findDistinctTipoObjeto();
    }
}
