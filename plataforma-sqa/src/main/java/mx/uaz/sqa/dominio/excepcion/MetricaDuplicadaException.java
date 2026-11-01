package mx.uaz.sqa.dominio.excepcion;

/**
 * Ya existe una métrica con el mismo nombre en el proyecto.
 */
public class MetricaDuplicadaException extends ReglaNegocioException {

    public MetricaDuplicadaException(String mensaje) {
        super(mensaje);
    }

    public MetricaDuplicadaException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
