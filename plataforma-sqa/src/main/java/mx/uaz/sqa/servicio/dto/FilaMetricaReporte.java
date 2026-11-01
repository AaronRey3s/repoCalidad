package mx.uaz.sqa.servicio.dto;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;

public record FilaMetricaReporte(String nombre, String unidad, String rangoVerde, String rangoAmarillo,
                                 String rangoRojo, String valorActual, EstadoSemaforo estado,
                                 List<FilaMedicionReporte> historial) {
}
