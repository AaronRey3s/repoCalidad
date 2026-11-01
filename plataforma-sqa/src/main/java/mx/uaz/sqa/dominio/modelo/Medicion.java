package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Medición capturada para una métrica (RS-04). Es un registro inmutable (RNF-03):
 * solo se crea a través de {@link Metrica#registrarMedicion} y nunca se actualiza.
 */
@Entity
@Immutable
@Table(name = "medicion")
public class Medicion extends RegistroAuditable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "metrica_id", nullable = false, updatable = false)
    private Metrica metrica;

    @Column(nullable = false, updatable = false)
    private double valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private EstadoSemaforo estado;

    @Column(length = 500, updatable = false)
    private String observacion;

    protected Medicion() {
        // Requerido por JPA
    }

    Medicion(Metrica metrica, double valor, EstadoSemaforo estado, String observacion,
             String autor, LocalDateTime registradoEn) {
        super(autor, registradoEn);
        this.metrica = metrica;
        this.valor = valor;
        this.estado = estado;
        this.observacion = Validaciones.opcional(observacion, "observacion", 500);
    }

    public String valorFormateado() {
        return metrica.getUnidad().formatear(valor);
    }

    public Metrica getMetrica() {
        return metrica;
    }

    public double getValor() {
        return valor;
    }

    public EstadoSemaforo getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }
}
