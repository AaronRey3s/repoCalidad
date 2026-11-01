package mx.uaz.sqa.dominio.excepcion;

/**
 * Se lanza cuando se solicita un proyecto, métrica, técnica o archivo que no existe.
 * La capa de presentación la traduce a una página 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException de(String recurso, Object id) {
        return new RecursoNoEncontradoException("No existe " + recurso + " con id " + id);
    }
}
