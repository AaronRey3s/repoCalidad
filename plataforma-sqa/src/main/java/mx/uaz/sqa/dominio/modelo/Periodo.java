package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.PeriodoInvalidoException;

/**
 * Periodo de evaluación SQA (RS-01): fecha de inicio, fecha de término y etiqueta.
 * Objeto de valor inmutable; solo se crea mediante {@link #de(String, LocalDate, LocalDate)},
 * que garantiza un rango de fechas válido (CU01 flujo 3a).
 */
@Embeddable
public class Periodo {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Column(name = "periodo_etiqueta", nullable = false, length = 60)
    private String etiqueta;

    @Column(name = "periodo_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "periodo_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    protected Periodo() {
        // Requerido por JPA
    }

    private Periodo(String etiqueta, LocalDate fechaInicio, LocalDate fechaFin) {
        this.etiqueta = etiqueta;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public static Periodo de(String etiqueta, LocalDate fechaInicio, LocalDate fechaFin) {
        String etiquetaLimpia = Validaciones.requerido(etiqueta,
                "La etiqueta del periodo es obligatoria", "etiquetaPeriodo", 60);
        if (fechaInicio == null) {
            throw new DatosInvalidosException("La fecha de inicio es obligatoria", "fechaInicio");
        }
        if (fechaFin == null) {
            throw new DatosInvalidosException("La fecha de fin es obligatoria", "fechaFin");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new PeriodoInvalidoException("La fecha de fin (" + fechaFin.format(FORMATO)
                    + ") no puede ser anterior a la fecha de inicio (" + fechaInicio.format(FORMATO) + ")",
                    "fechaFin");
        }
        return new Periodo(etiquetaLimpia, fechaInicio, fechaFin);
    }

    public boolean contiene(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public String descripcion() {
        return etiqueta + " (" + fechaInicio.format(FORMATO) + " – " + fechaFin.format(FORMATO) + ")";
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}
