package co.edu.uts.portal.bitacora.web;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * HU-22: visor global de la bitacora, solo ADMIN_TECNICO (URL + @PreAuthorize del
 * servicio). Filtros por tipo de objeto y rango de fecha; sin edicion ni borrado.
 */
@Controller
@RequestMapping("/admin/bitacora")
public class BitacoraAdminController {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final int TAMANO_PAGINA = 20;

    private final BitacoraService bitacoraService;

    public BitacoraAdminController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String tipoObjeto,
                        @RequestParam(required = false) LocalDate desde,
                        @RequestParam(required = false) LocalDate hasta,
                        @RequestParam(defaultValue = "0") int pagina,
                        Model model) {
        Instant desdeInstant = desde == null ? null : desde.atStartOfDay(ZONA).toInstant();
        Instant hastaInstant = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA).toInstant();

        var resultado = bitacoraService.buscar(vacioComoNull(tipoObjeto), desdeInstant, hastaInstant,
                PageRequest.of(pagina, TAMANO_PAGINA, Sort.by(Sort.Direction.DESC, "ocurridoEn")));

        model.addAttribute("registros", resultado);
        model.addAttribute("tiposObjeto", bitacoraService.tiposObjeto());
        model.addAttribute("tipoObjeto", tipoObjeto);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "admin/bitacora/lista";
    }

    private String vacioComoNull(String s) {
        return Optional.ofNullable(s).filter(v -> !v.isBlank()).orElse(null);
    }
}
