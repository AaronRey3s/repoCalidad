package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import mx.uaz.sqa.dominio.excepcion.UmbralesInvalidosException;

/**
 * Umbrales de aceptación de una métrica (RS-03): límite óptimo y límite crítico,
 * interpretados según el {@link SentidoMetrica}.
 *
 * <p>Ejemplo "mayor es mejor" con óptimo = 80 y crítico = 60:
 * Verde ≥ 80, Amarillo ≥ 60 y &lt; 80, Rojo &lt; 60.</p>
 */
@Embeddable
public class Umbrales {

    @Enumerated(EnumType.STRING)
    @Column(name = "umbral_sentido", nullable = false, length = 20)
    private SentidoMetrica sentido;

    @Column(name = "umbral_optimo", nullable = false)
    private double limiteOptimo;

    @Column(name = "umbral_critico", nullable = false)
    private double limiteCritico;

    protected Umbrales() {
        // Requerido por JPA
    }

    private Umbrales(SentidoMetrica sentido, double limiteOptimo, double limiteCritico) {
        this.sentido = sentido;
        this.limiteOptimo = limiteOptimo;
        this.limiteCritico = limiteCritico;
    }

    /**
     * Crea umbrales validando que estén completos, dentro del dominio de la unidad y que
     * no se crucen (CU02 paso 3 y flujo alterno 3a).
     */
    public static Umbrales definir(SentidoMetrica sentido, Double limiteOptimo, Double limiteCritico,
                                   UnidadMedida unidad) {
        if (sentido == null) {
            throw new UmbralesInvalidosException("Selecciona el sentido de la métrica", "sentido");
        }
        if (limiteOptimo == null || !Double.isFinite(limiteOptimo)) {
            throw new UmbralesInvalidosException("El límite óptimo es obligatorio y debe ser numérico", "limiteOptimo");
        }
        if (limiteCritico == null || !Double.isFinite(limiteCritico)) {
            throw new UmbralesInvalidosException("El límite crítico es obligatorio y debe ser numérico", "limiteCritico");
        }
        if (unidad != null && !unidad.admite(limiteOptimo)) {
            throw new UmbralesInvalidosException("El límite óptimo no es válido: " + unidad.reglaDeDominio(), "limiteOptimo");
        }
        if (unidad != null && !unidad.admite(limiteCritico)) {
            throw new UmbralesInvalidosException("El límite crítico no es válido: " + unidad.reglaDeDominio(), "limiteCritico");
        }
        if (!sentido.limitesCoherentes(limiteOptimo, limiteCritico)) {
            throw new UmbralesInvalidosException("Conflicto numérico en los umbrales: con \"" + sentido.getEtiqueta()
                    + "\" " + sentido.reglaDeCoherencia() + " (óptimo = " + limiteOptimo
                    + ", crítico = " + limiteCritico + ")", "limiteCritico");
        }
        return new Umbrales(sentido, limiteOptimo, limiteCritico);
    }

    /** RS-05: compara el valor contra los límites y devuelve el color del semáforo. */
    public EstadoSemaforo evaluar(double valor) {
        return sentido.evaluar(valor, limiteOptimo, limiteCritico);
    }

    public String rangoVerde(UnidadMedida unidad) {
        return sentido.rangoVerde(unidad.formatear(limiteOptimo), unidad.formatear(limiteCritico));
    }

    public String rangoAmarillo(UnidadMedida unidad) {
        return sentido.rangoAmarillo(unidad.formatear(limiteOptimo), unidad.formatear(limiteCritico));
    }

    public String rangoRojo(UnidadMedida unidad) {
        return sentido.rangoRojo(unidad.formatear(limiteOptimo), unidad.formatear(limiteCritico));
    }

    public SentidoMetrica getSentido() {
        return sentido;
    }

    public double getLimiteOptimo() {
        return limiteOptimo;
    }

    public double getLimiteCritico() {
        return limiteCritico;
    }
}
