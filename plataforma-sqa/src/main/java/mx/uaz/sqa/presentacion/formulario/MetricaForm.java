package mx.uaz.sqa.presentacion.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;
import mx.uaz.sqa.servicio.dto.DatosMetrica;

/**
 * Formulario de CU02 (configurar métrica y umbrales).
 */
public class MetricaForm {

    @NotBlank(message = "El nombre de la métrica es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @NotNull(message = "Selecciona la unidad de medida")
    private UnidadMedida unidad;

    @NotNull(message = "Selecciona la frecuencia")
    private Frecuencia frecuencia;

    @NotNull(message = "Selecciona el sentido de la métrica")
    private SentidoMetrica sentido = SentidoMetrica.MAYOR_ES_MEJOR;

    @NotNull(message = "El límite óptimo es obligatorio")
    private Double limiteOptimo;

    @NotNull(message = "El límite crítico es obligatorio")
    private Double limiteCritico;

    public DatosMetrica aDatos() {
        return new DatosMetrica(nombre, unidad, frecuencia, sentido, limiteOptimo, limiteCritico);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public UnidadMedida getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadMedida unidad) {
        this.unidad = unidad;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }

    public SentidoMetrica getSentido() {
        return sentido;
    }

    public void setSentido(SentidoMetrica sentido) {
        this.sentido = sentido;
    }

    public Double getLimiteOptimo() {
        return limiteOptimo;
    }

    public void setLimiteOptimo(Double limiteOptimo) {
        this.limiteOptimo = limiteOptimo;
    }

    public Double getLimiteCritico() {
        return limiteCritico;
    }

    public void setLimiteCritico(Double limiteCritico) {
        this.limiteCritico = limiteCritico;
    }
}
