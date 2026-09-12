package co.edu.uts.portal.analitica.web;

import co.edu.uts.portal.analitica.service.AnaliticaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * HU-23/HU-24: estadisticas agregadas y su exportacion. Solo ADMIN_FUNCIONAL (URL +
 * @PreAuthorize del servicio).
 */
@Controller
@RequestMapping("/admin/analitica")
public class AnaliticaAdminController {

    private final AnaliticaService analiticaService;

    public AnaliticaAdminController(AnaliticaService analiticaService) {
        this.analiticaService = analiticaService;
    }

    @GetMapping
    public String reporte(Model model) {
        model.addAttribute("reporte", analiticaService.generarReporte());
        return "admin/analitica/lista";
    }

    @GetMapping("/exportar.csv")
    public void exportar(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"estadisticas.csv\"");
        response.getWriter().write(analiticaService.generarCsv());
    }
}
