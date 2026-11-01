package mx.uaz.sqa.presentacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import mx.uaz.sqa.servicio.dto.IndicadorMetrica;
import mx.uaz.sqa.servicio.dto.PuntoSerie;

/**
 * Gráfica de tendencia histórica (RS-09) dibujada como SVG en el servidor.
 * No usa librerías ni CDN externos, así que funciona sin internet durante la demo.
 *
 * <p>El área de dibujo mide {@value #ANCHO} × {@value #ALTO} unidades; las coordenadas se
 * entregan ya formateadas para insertarse directamente en la plantilla.</p>
 */
public record GraficaTendencia(boolean vacia, String puntos, List<Marcador> marcadores,
                               String yOptimo, String yCritico, String etiquetaMaximo, String etiquetaMinimo) {

    public static final int ANCHO = 320;
    public static final int ALTO = 120;
    private static final double MARGEN_X = 14;
    private static final double MARGEN_Y = 12;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Punto dibujado en la gráfica, coloreado con el estado de semáforo de esa medición. */
    public record Marcador(String x, String y, String claseCss, String titulo) {
    }

    public static GraficaTendencia de(IndicadorMetrica indicador) {
        List<PuntoSerie> serie = indicador.serie();
        if (serie.isEmpty()) {
            return new GraficaTendencia(true, "", List.of(), "", "", "", "");
        }
        double[] escala = escala(indicador, serie);
        double minimo = escala[0];
        double maximo = escala[1];

        StringBuilder puntos = new StringBuilder();
        List<Marcador> marcadores = new ArrayList<>();
        int n = serie.size();
        for (int i = 0; i < n; i++) {
            PuntoSerie punto = serie.get(i);
            double x = n == 1 ? ANCHO / 2.0 : MARGEN_X + i * (ANCHO - 2 * MARGEN_X) / (n - 1);
            double y = y(punto.valor(), minimo, maximo);
            if (puntos.length() > 0) {
                puntos.append(' ');
            }
            puntos.append(coord(x)).append(',').append(coord(y));
            String titulo = punto.fecha().format(FECHA) + " · " + punto.valorTexto() + " · " + punto.estado().getEtiqueta();
            marcadores.add(new Marcador(coord(x), coord(y), punto.estado().getClaseCss(), titulo));
        }
        return new GraficaTendencia(false, puntos.toString(), marcadores,
                coord(y(indicador.limiteOptimo(), minimo, maximo)),
                coord(y(indicador.limiteCritico(), minimo, maximo)),
                eje(maximo), eje(minimo));
    }

    private static double[] escala(IndicadorMetrica indicador, List<PuntoSerie> serie) {
        if (indicador.esPorcentaje()) {
            return new double[] {0, 100};
        }
        DoubleSummaryStatistics valores = serie.stream().mapToDouble(PuntoSerie::valor).summaryStatistics();
        double minimo = Math.min(valores.getMin(), Math.min(indicador.limiteOptimo(), indicador.limiteCritico()));
        double maximo = Math.max(valores.getMax(), Math.max(indicador.limiteOptimo(), indicador.limiteCritico()));
        double holgura = (maximo - minimo) * 0.1;
        if (holgura == 0) {
            holgura = 1;
        }
        double minimoConHolgura = minimo - holgura;
        if (minimo >= 0 && minimoConHolgura < 0) {
            minimoConHolgura = 0;
        }
        return new double[] {minimoConHolgura, maximo + holgura};
    }

    private static double y(double valor, double minimo, double maximo) {
        double proporcion = (maximo - valor) / (maximo - minimo);
        return MARGEN_Y + proporcion * (ALTO - 2 * MARGEN_Y);
    }

    private static String coord(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    private static String eje(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
