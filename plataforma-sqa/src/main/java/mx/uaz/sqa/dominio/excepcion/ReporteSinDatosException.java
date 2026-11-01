package mx.uaz.sqa.dominio.excepcion;

/**
 * CU06 flujo 2a: no existen registros suficientes para emitir el reporte.
 */
public class ReporteSinDatosException extends ReglaNegocioException {

    public ReporteSinDatosException(String mensaje) {
        super(mensaje);
    }
}
