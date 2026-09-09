package co.edu.uts.portal.cuestionario.web;

import co.edu.uts.portal.cuestionario.service.VersionNoEditable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Traduce los intentos de editar/publicar una version invalida (HU-20) a un mensaje
 * flash y devuelve al usuario a la pagina anterior.
 */
@ControllerAdvice(basePackageClasses = CuestionarioErrorAdvice.class)
public class CuestionarioErrorAdvice {

    @ExceptionHandler(VersionNoEditable.class)
    public String versionNoEditable(VersionNoEditable e, RedirectAttributes ra, HttpServletRequest req) {
        ra.addFlashAttribute("error", e.getMessage());
        String referer = req.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/cuestionarios");
    }
}
