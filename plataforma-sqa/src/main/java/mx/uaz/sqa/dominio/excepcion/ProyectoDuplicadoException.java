package mx.uaz.sqa.dominio.excepcion;

/**
 * CU01 flujo 3b: ya existe un proyecto con el mismo nombre en el mismo periodo.
 */
public class ProyectoDuplicadoException extends ReglaNegocioException {

    public ProyectoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public ProyectoDuplicadoException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
