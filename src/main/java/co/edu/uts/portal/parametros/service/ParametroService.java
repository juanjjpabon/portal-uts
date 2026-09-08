package co.edu.uts.portal.parametros.service;

import co.edu.uts.portal.parametros.domain.Parametro;
import co.edu.uts.portal.parametros.repository.ParametroRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lectura y edicion de los parametros operativos (HU-27). La edicion exige
 * ADMIN_FUNCIONAL (HU-17, segunda capa sobre la regla de URL). {@link #valor(String)}
 * es el punto de consumo para el portal publico (HU-06, HU-07, HU-08, HU-11).
 */
@Service
public class ParametroService {

    private final ParametroRepository parametroRepository;

    public ParametroService(ParametroRepository parametroRepository) {
        this.parametroRepository = parametroRepository;
    }

    @Transactional(readOnly = true)
    public List<Parametro> listar() {
        return parametroRepository.findAllByOrderByGrupoAscOrdenAsc();
    }

    /** Parametros en orden, indexados por grupo, para pintar el formulario por secciones. */
    @Transactional(readOnly = true)
    public Map<String, List<Parametro>> agrupados() {
        Map<String, List<Parametro>> mapa = new LinkedHashMap<>();
        for (Parametro p : listar()) {
            mapa.computeIfAbsent(p.getGrupo(), g -> new java.util.ArrayList<>()).add(p);
        }
        return mapa;
    }

    /** Valor actual de una clave, o el default indicado si no esta configurada. */
    @Transactional(readOnly = true)
    public String valor(String clave, String porDefecto) {
        return parametroRepository.findByClave(clave)
                .map(Parametro::getValor)
                .filter(v -> v != null && !v.isBlank())
                .orElse(porDefecto);
    }

    public String valor(String clave) {
        return valor(clave, null);
    }

    @PreAuthorize("hasRole('ADMIN_FUNCIONAL')")
    @Transactional
    public int guardar(Map<Long, String> valoresPorId) {
        int cambios = 0;
        for (Parametro p : parametroRepository.findAllById(valoresPorId.keySet())) {
            String nuevo = normalizar(valoresPorId.get(p.getId()));
            if (!java.util.Objects.equals(nuevo, p.getValor())) {
                p.setValor(nuevo);
                cambios++;
            }
        }
        return cambios;
    }

    private String normalizar(String v) {
        if (v == null) {
            return null;
        }
        String t = v.strip();
        return t.isEmpty() ? null : t;
    }
}
