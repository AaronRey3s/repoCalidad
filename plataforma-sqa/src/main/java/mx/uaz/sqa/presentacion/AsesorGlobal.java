package mx.uaz.sqa.presentacion;

import java.security.Principal;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.servicio.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Atributos comunes a todas las vistas y manejo de recursos inexistentes (404).
 */
@ControllerAdvice
public class AsesorGlobal {

    private final UsuarioService usuarioService;

    public AsesorGlobal(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("usuarioActual")
    public UsuarioActual usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioService.buscarPorUsername(principal.getName())
                .map(u -> new UsuarioActual(u.getUsername(), u.getNombreCompleto(), u.getRol().getEtiqueta()))
                .orElseGet(() -> new UsuarioActual(principal.getName(), principal.getName(), ""));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoNoEncontrado(RecursoNoEncontradoException excepcion, Model model) {
        model.addAttribute("mensaje", excepcion.getMessage());
        return "error/404";
    }
}
