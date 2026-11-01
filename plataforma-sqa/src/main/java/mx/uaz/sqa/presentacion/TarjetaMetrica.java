package mx.uaz.sqa.presentacion;

import mx.uaz.sqa.servicio.dto.IndicadorMetrica;

/**
 * Indicador de métrica + su gráfica de tendencia, listo para la vista del dashboard.
 */
public record TarjetaMetrica(IndicadorMetrica indicador, GraficaTendencia grafica) {

    public static TarjetaMetrica de(IndicadorMetrica indicador) {
        return new TarjetaMetrica(indicador, GraficaTendencia.de(indicador));
    }
}
