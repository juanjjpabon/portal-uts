package co.edu.uts.portal.cuestionario.web;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.cuestionario.domain.CuestionarioVersion;
import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * HU-20: editor de una version (preguntas + niveles). Es de solo lectura si la
 * version ya no es BORRADOR.
 */
@Controller
@RequestMapping("/admin/cuestionarios/{id}/v/{numero}")
public class VersionEditorController {

    private final CuestionarioService cuestionarioService;
    private final RecursoService recursoService;

    public VersionEditorController(CuestionarioService cuestionarioService, RecursoService recursoService) {
        this.cuestionarioService = cuestionarioService;
        this.recursoService = recursoService;
    }

    @GetMapping
    public String editor(@PathVariable Long id, @PathVariable int numero, Model model) {
        CuestionarioVersion v = cuestionarioService.obtenerVersionCompleta(id, numero);
        model.addAttribute("cuestionario", v.getCuestionario());
        model.addAttribute("version", v);
        model.addAttribute("preguntas", v.getPreguntas());
        model.addAttribute("niveles", v.getNiveles());
        model.addAttribute("editable", v.esEditable());
        model.addAttribute("problemas", cuestionarioService.problemasParaPublicar(id, numero));
        // Ajuste Laura #3: solo se ofrecen rutas publicadas como "ruta aplicable" de un
        // nivel de resultado (una en borrador o archivada podria no ser visible o no
        // estar lista todavia).
        model.addAttribute("rutasDisponibles",
                recursoService.listar(TipoRecurso.RUTA, EstadoPublicacion.PUBLICADO, null, null));
        return "admin/cuestionarios/editor";
    }
}
