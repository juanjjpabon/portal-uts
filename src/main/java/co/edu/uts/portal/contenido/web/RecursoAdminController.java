package co.edu.uts.portal.contenido.web;

import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.ImagenProcesada;
import co.edu.uts.portal.contenido.domain.Recurso;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.service.ImagenInvalida;
import co.edu.uts.portal.contenido.service.ImagenService;
import co.edu.uts.portal.contenido.service.RecursoNoEncontrado;
import co.edu.uts.portal.contenido.service.CategoriaService;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.contenido.web.dto.RecursoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CRUD unico del panel para CONTENIDO / RUTA / CONTACTO (F-DC-125, HU-18/HU-19).
 * La seccion de la URL ({contenidos|rutas|contactos}) determina el tipo; el resto
 * del flujo es identico para los tres.
 */
@Controller
@RequestMapping("/admin/recursos/{seccion}")
public class RecursoAdminController {

    private final RecursoService recursoService;
    private final CategoriaService categoriaService;

    public RecursoAdminController(RecursoService recursoService, CategoriaService categoriaService) {
        this.recursoService = recursoService;
        this.categoriaService = categoriaService;
    }

    /** Traduce el segmento de URL a un tipo; 404 si no corresponde. */
    @ModelAttribute("tipo")
    public TipoRecurso tipo(@PathVariable String seccion) {
        return TipoRecurso.porSeccion(seccion)
                .orElseThrow(() -> new RecursoNoEncontrado("Seccion desconocida: " + seccion));
    }

    @ModelAttribute("seccion")
    public String seccion(@PathVariable String seccion) {
        return seccion;
    }

    @GetMapping
    public String lista(@ModelAttribute("tipo") TipoRecurso tipo,
                        @RequestParam(required = false) EstadoPublicacion estado,
                        @RequestParam(required = false) Long categoria,
                        @RequestParam(required = false) String q,
                        Model model) {
        List<Recurso> recursos = recursoService.listar(tipo, estado, categoria, q);
        model.addAttribute("recursos", recursos);
        if (tipo == TipoRecurso.RUTA) {
            model.addAttribute("conteoPasos", recursoService.contarPasos(recursos));
        }
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("estados", EstadoPublicacion.values());
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("filtroTexto", q);
        return "admin/recursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("tipo") TipoRecurso tipo, Model model) {
        model.addAttribute("form", RecursoForm.nuevo(tipo));
        return prepararFormulario(model);
    }

    @PostMapping
    public String crear(@ModelAttribute("tipo") TipoRecurso tipo,
                        @Valid @ModelAttribute("form") RecursoForm form,
                        BindingResult errores, Model model, RedirectAttributes ra) {
        form.setTipo(tipo);
        ImagenProcesada imagen = procesarImagen(form, errores);
        if (errores.hasErrors()) {
            return prepararFormulario(model);
        }
        Recurso creado = recursoService.crear(form, imagen);
        if (tipo == TipoRecurso.RUTA) {
            // Una ruta explica que hacer paso a paso: se lleva directo a agregar los pasos.
            ra.addFlashAttribute("ok", "Ruta creada. Ahora agrega los pasos que debe seguir la persona.");
            return "redirect:/admin/recursos/rutas/" + creado.getId() + "/pasos";
        }
        ra.addFlashAttribute("ok", tipo.getEtiquetaSingular() + " creado.");
        return "redirect:/admin/recursos/" + tipo.getSeccion() + "/" + creado.getId() + "/editar";
    }

    @GetMapping("/{id}/editar")
    public String editar(@ModelAttribute("tipo") TipoRecurso tipo, @PathVariable Long id, Model model) {
        model.addAttribute("form", RecursoForm.de(recursoService.obtenerDeTipo(id, tipo)));
        return prepararFormulario(model);
    }

    @PostMapping("/{id}")
    public String actualizar(@ModelAttribute("tipo") TipoRecurso tipo, @PathVariable Long id,
                             @Valid @ModelAttribute("form") RecursoForm form,
                             BindingResult errores, Model model, RedirectAttributes ra) {
        form.setTipo(tipo);
        form.setId(id);
        ImagenProcesada imagen = procesarImagen(form, errores);
        if (errores.hasErrors()) {
            return prepararFormulario(model);
        }
        recursoService.actualizar(id, form, imagen);
        ra.addFlashAttribute("ok", "Cambios guardados.");
        return "redirect:/admin/recursos/" + tipo.getSeccion() + "/" + id + "/editar";
    }

    @PostMapping("/{id}/publicar")
    public String publicar(@ModelAttribute("tipo") TipoRecurso tipo, @PathVariable Long id, RedirectAttributes ra) {
        recursoService.publicar(id, tipo);
        ra.addFlashAttribute("ok", "Publicado.");
        return listaRedirect(tipo);
    }

    @PostMapping("/{id}/archivar")
    public String archivar(@ModelAttribute("tipo") TipoRecurso tipo, @PathVariable Long id, RedirectAttributes ra) {
        recursoService.archivar(id, tipo);
        ra.addFlashAttribute("ok", "Archivado.");
        return listaRedirect(tipo);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@ModelAttribute("tipo") TipoRecurso tipo, @PathVariable Long id, RedirectAttributes ra) {
        recursoService.eliminar(id, tipo);
        ra.addFlashAttribute("ok", tipo.getEtiquetaSingular() + " eliminado.");
        return listaRedirect(tipo);
    }

    private String prepararFormulario(Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("estados", EstadoPublicacion.values());
        if (model.getAttribute("form") instanceof RecursoForm form
                && form.getId() != null && form.getTipo() == TipoRecurso.RUTA) {
            model.addAttribute("cantidadPasos", recursoService.contarPasosDeRuta(form.getId()));
        }
        return "admin/recursos/formulario";
    }

    /**
     * Procesa la imagen elegida (si hay). Si no se puede aceptar, deja el mensaje en el
     * campo imagenArchivo para que el formulario lo muestre y devuelve null.
     */
    private ImagenProcesada procesarImagen(RecursoForm form, BindingResult errores) {
        if (!form.tieneImagenNueva()) {
            return null;
        }
        try {
            return ImagenService.procesar(form.getImagenArchivo());
        } catch (ImagenInvalida e) {
            errores.rejectValue("imagenArchivo", "imagen.invalida", e.getMessage());
            return null;
        }
    }

    private String listaRedirect(TipoRecurso tipo) {
        return "redirect:/admin/recursos/" + tipo.getSeccion();
    }
}
