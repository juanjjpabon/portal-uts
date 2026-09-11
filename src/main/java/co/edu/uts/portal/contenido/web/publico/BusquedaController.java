package co.edu.uts.portal.contenido.web.publico;

import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.service.PortalPublicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** HU-03: busqueda publica por palabra clave sobre contenidos, rutas y contactos. */
@Controller
public class BusquedaController {

    private final PortalPublicoService servicio;

    public BusquedaController(PortalPublicoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String q, Model model) {
        List<Recurso> encontrados = servicio.buscar(q);
        Map<TipoRecurso, List<Recurso>> agrupados = new LinkedHashMap<>();
        for (TipoRecurso t : TipoRecurso.values()) {
            List<Recurso> deEsteTipo = encontrados.stream().filter(r -> r.getTipo() == t).toList();
            if (!deEsteTipo.isEmpty()) {
                agrupados.put(t, deEsteTipo);
            }
        }
        model.addAttribute("q", q);
        model.addAttribute("resultados", agrupados);
        model.addAttribute("total", encontrados.size());
        return "publico/busqueda/resultados";
    }
}
