package mx.uaz.sqa.dominio.modelo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio que calcula el índice global de cumplimiento y el dictamen de salud
 * (RS-09, RS-10, CU06 paso 2).
 *
 * <p><b>Fórmula (decisión de diseño del equipo, ajustable):</b></p>
 * <ul>
 *   <li>Puntaje por métrica con datos: Verde = 100, Amarillo = 50, Rojo = 0.</li>
 *   <li>Índice de métricas = promedio de los puntajes de las métricas con al menos una medición.</li>
 *   <li>Porcentaje de técnicas = técnicas en estado Aplicado / técnicas asignadas × 100.</li>
 *   <li>Índice global = promedio de los componentes que tengan datos.</li>
 *   <li>Dictamen: ≥ 80 Saludable, ≥ 60 En riesgo, &lt; 60 Crítico; sin componentes = Sin datos.</li>
 * </ul>
 */
public class CalculadoraSalud {

    public static final double UMBRAL_SALUDABLE = 80.0;
    public static final double UMBRAL_EN_RIESGO = 60.0;

    public IndiceSalud calcular(Collection<EstadoSemaforo> estadosMetricas,
                                Collection<EstadoTecnica> estadosTecnicas) {
        Double indiceMetricas = indiceMetricas(estadosMetricas);
        Double porcentajeTecnicas = porcentajeTecnicasAplicadas(estadosTecnicas);
        Double indiceGlobal = promedioDisponible(indiceMetricas, porcentajeTecnicas);
        return new IndiceSalud(redondear(indiceMetricas), redondear(porcentajeTecnicas),
                redondear(indiceGlobal), dictaminar(indiceGlobal));
    }

    public Double indiceMetricas(Collection<EstadoSemaforo> estados) {
        if (estados == null) {
            return null;
        }
        List<EstadoSemaforo> conDatos = estados.stream()
                .filter(Objects::nonNull)
                .filter(EstadoSemaforo::tieneDatos)
                .toList();
        if (conDatos.isEmpty()) {
            return null;
        }
        return conDatos.stream().mapToInt(EstadoSemaforo::getPuntaje).average().orElse(0);
    }

    public Double porcentajeTecnicasAplicadas(Collection<EstadoTecnica> estados) {
        if (estados == null || estados.isEmpty()) {
            return null;
        }
        long aplicadas = estados.stream().filter(e -> e == EstadoTecnica.APLICADO).count();
        return aplicadas * 100.0 / estados.size();
    }

    public Dictamen dictaminar(Double indiceGlobal) {
        if (indiceGlobal == null) {
            return Dictamen.SIN_DATOS;
        }
        if (indiceGlobal >= UMBRAL_SALUDABLE) {
            return Dictamen.SALUDABLE;
        }
        if (indiceGlobal >= UMBRAL_EN_RIESGO) {
            return Dictamen.EN_RIESGO;
        }
        return Dictamen.CRITICO;
    }

    private static Double promedioDisponible(Double a, Double b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return (a + b) / 2.0;
    }

    private static Double redondear(Double valor) {
        return valor == null ? null : Math.round(valor * 10.0) / 10.0;
    }
}
