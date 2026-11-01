package mx.uaz.sqa.dominio.modelo;

/**
 * Resultado del cálculo de salud de un proyecto.
 *
 * @param indiceMetricas              promedio de puntajes de las métricas con datos (0–100), o {@code null}
 * @param porcentajeTecnicasAplicadas porcentaje de técnicas SQA en estado Aplicado (0–100), o {@code null}
 * @param indiceGlobal                índice global de cumplimiento (0–100), o {@code null} si no hay datos
 * @param dictamen                    dictamen global de salud
 */
public record IndiceSalud(Double indiceMetricas, Double porcentajeTecnicasAplicadas,
                          Double indiceGlobal, Dictamen dictamen) {

    public boolean tieneDatos() {
        return indiceGlobal != null;
    }

    public String indiceMetricasTexto() {
        return formatear(indiceMetricas);
    }

    public String porcentajeTecnicasTexto() {
        return formatear(porcentajeTecnicasAplicadas);
    }

    public String indiceGlobalTexto() {
        return formatear(indiceGlobal);
    }

    private static String formatear(Double valor) {
        return valor == null ? "Sin datos" : UnidadMedida.PORCENTAJE.formatear(valor);
    }
}
