package mx.uaz.sqa.dominio.excepcion;

/**
 * CU03 flujo 2a: el valor capturado no es numérico o está fuera del dominio de su unidad.
 */
public class ValorFueraDeDominioException extends ReglaNegocioException {

    public ValorFueraDeDominioException(String mensaje) {
        super(mensaje);
    }

    public ValorFueraDeDominioException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
