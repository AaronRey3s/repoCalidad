package mx.uaz.sqa.dominio.excepcion;

/**
 * Un dato obligatorio falta o excede el tamaño permitido (CU01 flujo 3a: datos incompletos).
 */
public class DatosInvalidosException extends ReglaNegocioException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
