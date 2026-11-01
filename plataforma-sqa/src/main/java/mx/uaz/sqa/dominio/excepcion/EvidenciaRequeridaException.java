package mx.uaz.sqa.dominio.excepcion;

/**
 * CU04 flujo 3a / RS-08: no se puede marcar una técnica como Aplicado sin evidencia.
 */
public class EvidenciaRequeridaException extends ReglaNegocioException {

    public EvidenciaRequeridaException(String mensaje) {
        super(mensaje);
    }

    public EvidenciaRequeridaException(String mensaje, String campo) {
        super(mensaje, campo);
    }
}
