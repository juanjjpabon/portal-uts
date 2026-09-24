package co.edu.uts.portal.common.web;

import co.edu.uts.portal.bitacora.service.BitacoraService;
import co.edu.uts.portal.contenido.domain.EstadoPublicacion;
import co.edu.uts.portal.contenido.domain.TipoRecurso;
import co.edu.uts.portal.contenido.service.RecursoService;
import co.edu.uts.portal.cuestionario.service.CuestionarioService;
import co.edu.uts.portal.identidad.service.UsuarioAutenticado;
import co.edu.uts.portal.identidad.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Panel de administracion (HU-16/HU-17). Ajuste Laura #6: el tablero muestra
 * indicadores reales en vez de estar vacio (publicados/borrador de cada tipo de
 * recurso, cuestionarios activos/publicados y, solo para ADMIN_TECNICO, usuarios
 * activos y las ultimas acciones de la bitacora).
 *
 * Los conteos de contenido no llevan @PreAuthorize (lecturas abiertas al panel,
 * igual que en RecursoService/CuestionarioService); los indicadores de usuarios y
 * bitacora se calculan solo si el rol es ADMIN_TECNICO, para no mostrarle a
 * ADMIN_FUNCIONAL datos que su propio menu no expone (/admin/usuarios y
 * /admin/bitacora ya estan restringidos a ADMIN_TECNICO).
 *
 * Como el resto de los controladores del panel, este habla solo con la capa de
 * Service (nunca con un Repository directo).
 */
@Controller
public class PanelController {

    private static final int ULTIMAS_ACCIONES = 8;

    private final RecursoService recursoService;
    private final CuestionarioService cuestionarioService;
    private final UsuarioService usuarioService;
    private final BitacoraService bitacora;

    public PanelController(RecursoService recursoService, CuestionarioService cuestionarioService,
                           UsuarioService usuarioService, BitacoraService bitacora) {
        this.recursoService = recursoService;
        this.cuestionarioService = cuestionarioService;
        this.usuarioService = usuarioService;
        this.bitacora = bitacora;
    }

    @GetMapping("/admin")
    public String dashboard(@AuthenticationPrincipal UsuarioAutenticado principal, HttpServletRequest request,
                            Model model) {
        model.addAttribute("correo", principal.getCorreo());

        model.addAttribute("contenidosPublicados", contar(TipoRecurso.CONTENIDO, EstadoPublicacion.PUBLICADO));
        model.addAttribute("contenidosBorrador", contar(TipoRecurso.CONTENIDO, EstadoPublicacion.BORRADOR));
        model.addAttribute("rutasPublicadas", contar(TipoRecurso.RUTA, EstadoPublicacion.PUBLICADO));
        model.addAttribute("rutasBorrador", contar(TipoRecurso.RUTA, EstadoPublicacion.BORRADOR));
        model.addAttribute("contactosPublicados", contar(TipoRecurso.CONTACTO, EstadoPublicacion.PUBLICADO));
        model.addAttribute("contactosBorrador", contar(TipoRecurso.CONTACTO, EstadoPublicacion.BORRADOR));

        model.addAttribute("cuestionariosActivos", cuestionarioService.contarActivos());
        model.addAttribute("cuestionariosPublicados", cuestionarioService.contarPublicados());

        if (request.isUserInRole("ADMIN_TECNICO")) {
            model.addAttribute("usuariosActivos", usuarioService.contarActivos());
            model.addAttribute("ultimasAcciones", bitacora.ultimos(ULTIMAS_ACCIONES));
        }
        return "admin/dashboard";
    }

    private long contar(TipoRecurso tipo, EstadoPublicacion estado) {
        return recursoService.contarPorTipoYEstado(tipo, estado);
    }
}
