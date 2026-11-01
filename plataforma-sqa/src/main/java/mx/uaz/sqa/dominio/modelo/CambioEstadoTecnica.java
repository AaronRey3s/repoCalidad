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
 * Bitácora inmutable de cambios de estado de una técnica SQA (RNF-03 sobre RS-07).
 */
@Entity
@Immutable
@Table(name = "cambio_estado_tecnica")
public class CambioEstadoTecnica extends RegistroAuditable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "tecnica_id", nullable = false, updatable = false)
    private TecnicaSqa tecnica;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20, updatable = false)
    private EstadoTecnica estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20, updatable = false)
    private EstadoTecnica estadoNuevo;

    @Column(length = 1000, updatable = false)
    private String detalle;

    protected CambioEstadoTecnica() {
        // Requerido por JPA
    }

    CambioEstadoTecnica(TecnicaSqa tecnica, EstadoTecnica estadoAnterior, EstadoTecnica estadoNuevo,
                        String autor, LocalDateTime registradoEn, String detalle) {
        super(autor, registradoEn);
        this.tecnica = tecnica;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.detalle = detalle == null ? null : detalle.substring(0, Math.min(detalle.length(), 1000));
    }

    public TecnicaSqa getTecnica() {
        return tecnica;
    }

    public EstadoTecnica getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoTecnica getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getDetalle() {
        return detalle;
    }
}
