package mx.uaz.sqa.servicio.reporte;

import mx.uaz.sqa.servicio.dto.ReporteSalud;

/**
 * Estrategia de exportación del reporte de salud. Hoy existe la implementación PDF (RS-11);
 * se pueden agregar otras (CSV, Excel) sin tocar el servicio (principio abierto/cerrado).
 */
public interface GeneradorReporte {

    byte[] generar(ReporteSalud reporte);
}
