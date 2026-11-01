package mx.uaz.sqa.servicio.dto;

import java.time.LocalDate;

/**
 * Datos de entrada de CU01 (crear/editar proyecto).
 */
public record DatosProyecto(String nombre, String descripcion, String tecnologias,
                            String etiquetaPeriodo, LocalDate fechaInicio, LocalDate fechaFin) {
}
