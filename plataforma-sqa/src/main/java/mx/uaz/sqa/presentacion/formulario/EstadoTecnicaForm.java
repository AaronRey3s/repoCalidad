package mx.uaz.sqa.presentacion.formulario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;

/**
 * Formulario de RS-07 / RS-08 (cambiar estado y adjuntar evidencia).
 * El archivo se recibe aparte como MultipartFile.
 */
public class EstadoTecnicaForm {

    @NotNull(message = "Selecciona el nuevo estado")
    private EstadoTecnica estado;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String evidenciaUrl;

    public EstadoTecnica getEstado() {
        return estado;
    }

    public void setEstado(EstadoTecnica estado) {
        this.estado = estado;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public void setEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }
}
