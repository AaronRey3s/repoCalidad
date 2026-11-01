package mx.uaz.sqa.dominio.excepcion;

/**
 * CU02 flujo 3a: los límites de los umbrales se cruzan o son incoherentes.
 */
public class UmbralesInvalidosException extends ReglaNegocioException {

    public UmbralesInvalidosException(String mensaje) {
        super(mensaje);
    }

    public UmbralesInvalidosException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
