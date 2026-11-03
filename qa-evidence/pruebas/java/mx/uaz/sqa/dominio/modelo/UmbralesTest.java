package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uaz.sqa.dominio.excepcion.UmbralesInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Umbrales y cálculo de semáforo (RS-03, RS-05, CU02 flujo 3a)")
class UmbralesTest {

    private final Umbrales mayorEsMejor =
            Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0, UnidadMedida.PORCENTAJE);
    private final Umbrales menorEsMejor =
            Umbrales.definir(SentidoMetrica.MENOR_ES_MEJOR, 1.0, 3.0, UnidadMedida.RAZON);

    @ParameterizedTest(name = "mayor es mejor: valor {0} → {1}")
    @CsvSource({
            "100, VERDE",
            "80, VERDE",
            "79.99, AMARILLO",
            "60, AMARILLO",
            "59.99, ROJO",
            "0, ROJO"
    })
    @DisplayName("Análisis de valores límite con 'mayor es mejor' (80 / 60)")
    void evaluaMayorEsMejor(double valor, EstadoSemaforo esperado) {
        assertEquals(esperado, mayorEsMejor.evaluar(valor));
    }

    @ParameterizedTest(name = "menor es mejor: valor {0} → {1}")
    @CsvSource({
            "0, VERDE",
            "1, VERDE",
            "1.01, AMARILLO",
            "3, AMARILLO",
            "3.01, ROJO",
            "10, ROJO"
    })
    @DisplayName("Análisis de valores límite con 'menor es mejor' (1 / 3)")
    void evaluaMenorEsMejor(double valor, EstadoSemaforo esperado) {
        assertEquals(esperado, menorEsMejor.evaluar(valor));
    }

    @Test
    @DisplayName("Dados límites cruzados en 'mayor es mejor', entonces se rechaza indicando el conflicto")
    void rechazaCruceMayorEsMejor() {
        UmbralesInvalidosException error = assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, 60.0, 80.0, UnidadMedida.PORCENTAJE));

        assertEquals("limiteCritico", error.getCampo());
        assertTrue(error.getMessage().contains("MAYOR"));
    }

    @Test
    @DisplayName("Dados límites cruzados en 'menor es mejor', entonces se rechaza")
    void rechazaCruceMenorEsMejor() {
        assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MENOR_ES_MEJOR, 5.0, 2.0, UnidadMedida.RAZON));
    }

    @Test
    @DisplayName("Límites iguales generan solapamiento y se rechazan")
    void rechazaLimitesIguales() {
        assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, 70.0, 70.0, UnidadMedida.PORCENTAJE));
    }

    @Test
    @DisplayName("Un porcentaje fuera de [0, 100] no es un límite válido")
    void rechazaLimitesFueraDelDominio() {
        assertEquals("limiteOptimo", assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, 120.0, 60.0, UnidadMedida.PORCENTAJE)).getCampo());
        assertEquals("limiteCritico", assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MENOR_ES_MEJOR, 0.0, -1.0, UnidadMedida.CONTEO)).getCampo());
    }

    @Test
    @DisplayName("Faltan sentido o límites")
    void rechazaDatosFaltantes() {
        assertEquals("sentido", assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(null, 80.0, 60.0, UnidadMedida.PORCENTAJE)).getCampo());
        assertEquals("limiteOptimo", assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, null, 60.0, UnidadMedida.PORCENTAJE)).getCampo());
        assertEquals("limiteCritico", assertThrows(UmbralesInvalidosException.class,
                () -> Umbrales.definir(SentidoMetrica.MAYOR_ES_MEJOR, 80.0, Double.NaN, UnidadMedida.PORCENTAJE)).getCampo());
    }

    @Test
    @DisplayName("Los rangos se describen de forma continua y sin huecos")
    void describeRangos() {
        assertEquals("≥ 80 %", mayorEsMejor.rangoVerde(UnidadMedida.PORCENTAJE));
        assertEquals("≥ 60 % y < 80 %", mayorEsMejor.rangoAmarillo(UnidadMedida.PORCENTAJE));
        assertEquals("< 60 %", mayorEsMejor.rangoRojo(UnidadMedida.PORCENTAJE));
        assertEquals("≤ 1", menorEsMejor.rangoVerde(UnidadMedida.RAZON));
        assertEquals("> 1 y ≤ 3", menorEsMejor.rangoAmarillo(UnidadMedida.RAZON));
        assertEquals("> 3", menorEsMejor.rangoRojo(UnidadMedida.RAZON));
    }

    @Test
    @DisplayName("Los getters exponen la configuración")
    void exponeConfiguracion() {
        assertEquals(SentidoMetrica.MAYOR_ES_MEJOR, mayorEsMejor.getSentido());
        assertEquals(80.0, mayorEsMejor.getLimiteOptimo());
        assertEquals(60.0, mayorEsMejor.getLimiteCritico());
    }
}
