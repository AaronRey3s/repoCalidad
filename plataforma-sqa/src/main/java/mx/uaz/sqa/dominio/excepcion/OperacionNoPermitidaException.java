package mx.uaz.sqa.dominio.excepcion;

/**
 * La operación solicitada viola una regla de integridad del sistema.
 */
public class OperacionNoPermitidaException extends ReglaNegocioException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
