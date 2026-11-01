package mx.uaz.sqa.servicio.dto;

import java.time.LocalDateTime;
import java.util.List;
import mx.uaz.sqa.dominio.modelo.IndiceSalud;

/**
 * Estructura consolidada del reporte de salud (RS-10): datos generales del periodo,
 * resultados de métricas, técnicas SQA con evidencias e índice global de cumplimiento.
 */
public record ReporteSalud(ProyectoResumen proyecto, LocalDateTime emitidoEn, String emitidoPor,
                           List<FilaMetricaReporte> metricas, List<FilaTecnicaReporte> tecnicas,
                           IndiceSalud salud) {
}
