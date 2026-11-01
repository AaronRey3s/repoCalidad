package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Índice global de cumplimiento y dictamen de salud (RS-09, RS-10)")
class CalculadoraSaludTest {

    private final CalculadoraSalud calculadora = new CalculadoraSalud();

    @Test
    @DisplayName("Sin métricas con datos ni técnicas, el dictamen es 'Sin datos'")
    void sinDatos() {
        IndiceSalud salud = calculadora.calcular(List.of(EstadoSemaforo.SIN_DATOS), List.of());

        assertNull(salud.indiceMetricas());
        assertNull(salud.porcentajeTecnicasAplicadas());
        assertNull(salud.indiceGlobal());
        assertEquals(Dictamen.SIN_DATOS, salud.dictamen());
        assertFalse(salud.tieneDatos());
        assertEquals("Sin datos", salud.indiceGlobalTexto());
    }

    @Test
    @DisplayName("Todo en verde y todas las técnicas aplicadas → 100 % Saludable")
    void todoSaludable() {
        IndiceSalud salud = calculadora.calcular(
                List.of(EstadoSemaforo.VERDE, EstadoSemaforo.VERDE),
                List.of(EstadoTecnica.APLICADO, EstadoTecnica.APLICADO));

        assertEquals(100.0, salud.indiceGlobal());
        assertEquals(Dictamen.SALUDABLE, salud.dictamen());
        assertTrue(salud.tieneDatos());
        assertEquals("100 %", salud.indiceGlobalTexto());
    }

    @Test
    @DisplayName("Combinación de métricas y técnicas: promedio de ambos componentes")
    void combinacion() {
        // Métricas: (100 + 50 + 0) / 3 = 50 ; técnicas: 1 de 4 = 25 ; global = 37.5 → Crítico
        IndiceSalud salud = calculadora.calcular(
                List.of(EstadoSemaforo.VERDE, EstadoSemaforo.AMARILLO, EstadoSemaforo.ROJO, EstadoSemaforo.SIN_DATOS),
                List.of(EstadoTecnica.APLICADO, EstadoTecnica.PENDIENTE, EstadoTecnica.EN_PROGRESO, EstadoTecnica.FALLIDO));

        assertEquals(50.0, salud.indiceMetricas());
        assertEquals(25.0, salud.porcentajeTecnicasAplicadas());
        assertEquals(37.5, salud.indiceGlobal());
        assertEquals(Dictamen.CRITICO, salud.dictamen());
        assertEquals("50 %", salud.indiceMetricasTexto());
        assertEquals("25 %", salud.porcentajeTecnicasTexto());
    }

    @Test
    @DisplayName("Si solo hay técnicas, el índice global es su porcentaje")
    void soloTecnicas() {
        IndiceSalud salud = calculadora.calcular(List.of(), List.of(EstadoTecnica.APLICADO, EstadoTecnica.PENDIENTE,
                EstadoTecnica.APLICADO));

        assertEquals(66.7, salud.indiceGlobal());
        assertEquals(Dictamen.EN_RIESGO, salud.dictamen());
    }

    @Test
    @DisplayName("Si solo hay métricas, el índice global es el índice de métricas")
    void soloMetricas() {
        IndiceSalud salud = calculadora.calcular(List.of(EstadoSemaforo.AMARILLO), null);

        assertEquals(50.0, salud.indiceGlobal());
        assertEquals(Dictamen.CRITICO, salud.dictamen());
    }

    @Test
    @DisplayName("Valores límite del dictamen: 80 Saludable, 79.9 En riesgo, 60 En riesgo, 59.9 Crítico")
    void limitesDelDictamen() {
        assertEquals(Dictamen.SALUDABLE, calculadora.dictaminar(80.0));
        assertEquals(Dictamen.EN_RIESGO, calculadora.dictaminar(79.9));
        assertEquals(Dictamen.EN_RIESGO, calculadora.dictaminar(60.0));
        assertEquals(Dictamen.CRITICO, calculadora.dictaminar(59.9));
        assertEquals(Dictamen.SIN_DATOS, calculadora.dictaminar(null));
    }

    @Test
    @DisplayName("Entradas nulas se tratan como ausencia de datos")
    void entradasNulas() {
        assertNull(calculadora.indiceMetricas(null));
        assertNull(calculadora.porcentajeTecnicasAplicadas(null));
        assertEquals(Dictamen.SIN_DATOS, calculadora.calcular(null, null).dictamen());
    }
}
