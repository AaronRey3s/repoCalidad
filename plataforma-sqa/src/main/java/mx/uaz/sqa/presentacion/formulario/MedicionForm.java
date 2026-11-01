package mx.uaz.sqa.presentacion.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de CU03. El valor se recibe como texto para que el dominio valide
 * explícitamente que sea numérico (flujo alterno 2a).
 */
public class MedicionForm {

    @NotBlank(message = "Ingresa el valor obtenido")
    @Size(max = 30, message = "Valor demasiado largo")
    private String valor;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String observacion;

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
