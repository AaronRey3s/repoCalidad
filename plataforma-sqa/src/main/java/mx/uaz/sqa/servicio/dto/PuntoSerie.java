package mx.uaz.sqa.servicio.dto;

import java.time.LocalDateTime;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;

/**
 * Punto de la serie histórica de una métrica (gráfica de tendencia, RS-09).
 */
public record PuntoSerie(LocalDateTime fecha, double valor, String valorTexto, EstadoSemaforo estado) {
}
