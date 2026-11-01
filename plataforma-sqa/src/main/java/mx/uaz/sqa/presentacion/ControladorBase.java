package mx.uaz.sqa.presentacion;

import mx.uaz.sqa.dominio.excepcion.ReglaNegocioException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Comportamiento común de los controladores: traducir una violación de regla de negocio
 * en un error visible del formulario y publicar mensajes de éxito o error.
 */
public abstract class ControladorBase {

    protected static final String ATRIBUTO_EXITO = "exito";
    protected static final String ATRIBUTO_ERROR = "error";

    /**
     * Si la excepción indica un campo, el error se asocia a ese campo para resaltarlo
     * (CU01 flujo 3a); en otro caso se muestra como error general del formulario.
     */
    protected void registrarError(BindingResult resultado, ReglaNegocioException error) {
        if (error.tieneCampo()) {
            try {
                resultado.rejectValue(error.getCampo(), "reglaNegocio", error.getMessage());
                return;
            } catch (RuntimeException campoInexistenteEnFormulario) {
                // El campo no existe en este formulario: se reporta como error general.
            }
        }
        resultado.reject("reglaNegocio", error.getMessage());
    }

    protected void exito(RedirectAttributes atributos, String mensaje) {
        atributos.addFlashAttribute(ATRIBUTO_EXITO, mensaje);
    }

    protected void error(RedirectAttributes atributos, String mensaje) {
        atributos.addFlashAttribute(ATRIBUTO_ERROR, mensaje);
    }
}
