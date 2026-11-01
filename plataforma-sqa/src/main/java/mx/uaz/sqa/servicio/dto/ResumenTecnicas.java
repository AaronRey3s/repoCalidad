package mx.uaz.sqa.servicio.dto;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;

/**
 * Cobertura de técnicas SQA del proyecto (RS-07, RS-09).
 */
public record ResumenTecnicas(int total, int pendientes, int enProgreso, int aplicadas, int fallidas,
                              Double porcentajeAplicadas) {

    public static ResumenTecnicas desde(List<TecnicaSqa> tecnicas, Double porcentajeAplicadas) {
        return new ResumenTecnicas(tecnicas.size(),
                contar(tecnicas, EstadoTecnica.PENDIENTE),
                contar(tecnicas, EstadoTecnica.EN_PROGRESO),
                contar(tecnicas, EstadoTecnica.APLICADO),
                contar(tecnicas, EstadoTecnica.FALLIDO),
                porcentajeAplicadas);
    }

    private static int contar(List<TecnicaSqa> tecnicas, EstadoTecnica estado) {
        return (int) tecnicas.stream().filter(t -> t.getEstado() == estado).count();
    }

    public String porcentajeTexto() {
        return porcentajeAplicadas == null ? "Sin técnicas" : UnidadMedida.PORCENTAJE.formatear(porcentajeAplicadas);
    }

    /** Ancho (0–100) de la barra de progreso en la vista. */
    public long anchoBarra() {
        return porcentajeAplicadas == null ? 0 : Math.round(porcentajeAplicadas);
    }
}
