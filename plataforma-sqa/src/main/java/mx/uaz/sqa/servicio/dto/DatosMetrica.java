package mx.uaz.sqa.servicio.dto;

import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;

/**
 * Datos de entrada de CU02 (configurar métrica).
 */
public record DatosMetrica(String nombre, UnidadMedida unidad, Frecuencia frecuencia,
                           SentidoMetrica sentido, Double limiteOptimo, Double limiteCritico) {
}
