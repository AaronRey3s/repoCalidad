package mx.uaz.sqa.dominio.excepcion;

/**
 * Precondición de CU02, CU03 y CU04: el proyecto debe estar activo para registrar cambios.
 */
public class ProyectoInactivoException extends ReglaNegocioException {

    public ProyectoInactivoException(String mensaje) {
        super(mensaje);
    }
}
