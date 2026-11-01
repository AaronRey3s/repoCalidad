package mx.uaz.sqa.servicio.dto;

import mx.uaz.sqa.dominio.modelo.EstadoTecnica;

public record FilaTecnicaReporte(String nombre, String categoria, EstadoTecnica estado,
                                 String responsable, String evidencia) {
}
