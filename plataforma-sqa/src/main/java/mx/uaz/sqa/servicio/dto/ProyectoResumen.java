package mx.uaz.sqa.servicio.dto;

import java.time.LocalDate;
import mx.uaz.sqa.dominio.modelo.Proyecto;

/**
 * Vista de solo lectura de un proyecto para la capa de presentación.
 */
public record ProyectoResumen(Long id, String nombre, String descripcion, String tecnologias,
                              String periodo, LocalDate fechaInicio, LocalDate fechaFin,
                              String periodoDescripcion, boolean activo) {

    public static ProyectoResumen desde(Proyecto proyecto) {
        return new ProyectoResumen(proyecto.getId(), proyecto.getNombre(), proyecto.getDescripcion(),
                proyecto.getTecnologias(), proyecto.getPeriodo().getEtiqueta(),
                proyecto.getPeriodo().getFechaInicio(), proyecto.getPeriodo().getFechaFin(),
                proyecto.getPeriodo().descripcion(), proyecto.isActivo());
    }
}
