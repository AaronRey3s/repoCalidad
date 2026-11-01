package mx.uaz.sqa.servicio.dto;

/**
 * Archivo listo para descargarse (evidencia o reporte PDF).
 */
public record ArchivoDescargable(String nombre, byte[] contenido) {
}
