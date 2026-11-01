package mx.uaz.sqa.dominio.excepcion;

/**
 * La URL o el archivo de evidencia no son válidos.
 */
public class EvidenciaInvalidaException extends ReglaNegocioException {

    public EvidenciaInvalidaException(String mensaje) {
        super(mensaje);
    }

    public EvidenciaInvalidaException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
