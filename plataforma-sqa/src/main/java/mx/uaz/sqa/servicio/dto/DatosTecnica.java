package mx.uaz.sqa.servicio.dto;

import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;

/**
 * Datos de entrada de RS-06 (asignar técnica SQA).
 */
public record DatosTecnica(String nombre, CategoriaTecnica categoria, String descripcion, String responsable) {
}
