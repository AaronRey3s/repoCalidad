package mx.uaz.sqa.dominio.excepcion;

/**
 * CU01 flujo 3a: el rango de fechas del periodo es inválido (fin anterior al inicio).
 */
public class PeriodoInvalidoException extends ReglaNegocioException {

    public PeriodoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public PeriodoInvalidoException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
