package mx.uaz.sqa.dominio.excepcion;

/**
 * Excepción base de toda violación a una regla de negocio del SRS.
 *
 * <p>Opcionalmente indica el <em>campo</em> del formulario que originó el error, para que
 * la capa de presentación lo resalte (CU01 flujo 3a: "el sistema resalta los campos con error").
 * Las subclases especializan el tipo de regla violada (herencia + polimorfismo).</p>
 */
public class ReglaNegocioException extends RuntimeException {

    private final String campo;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, null);
    }

    public ReglaNegocioException(String mensaje, String campo) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }

    public boolean tieneCampo() {
        return campo != null && !campo.isBlank();
    }
}
