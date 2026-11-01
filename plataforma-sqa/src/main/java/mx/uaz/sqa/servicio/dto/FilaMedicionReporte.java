package mx.uaz.sqa.servicio.dto;

import java.time.LocalDateTime;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;

public record FilaMedicionReporte(LocalDateTime fecha, String valor, EstadoSemaforo estado, String autor) {
}
