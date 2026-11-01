package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Conversión del valor capturado (CU03 flujo 2a: caracteres no numéricos)")
class ValorNumericoTest {

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "85, 85.0",
            "85.5, 85.5",
            "'85,5', 85.5",
            "'  7  ', 7.0",
            "-3.25, -3.25",
            "+4, 4.0"
    })
    @DisplayName("Partición válida: números con punto o coma decimal")
    void convierteNumerosValidos(String texto, double esperado) {
        assertEquals(esperado, ValorNumerico.desdeTexto(texto, "valor"), 0.0001);
    }

    @ParameterizedTest(name = "\"{0}\" se rechaza")
    @CsvSource({"abc", "12a", "1e5", "NaN", "Infinity", "1.2.3", "'--1'", "'.5'"})
    @DisplayName("Partición inválida: caracteres no numéricos")
    void rechazaTextoNoNumerico(String texto) {
        ValorFueraDeDominioException error = assertThrows(ValorFueraDeDominioException.class,
                () -> ValorNumerico.desdeTexto(texto, "valor"));
        assertEquals("valor", error.getCampo());
    }

    @Test
    @DisplayName("Valor vacío o nulo se rechaza")
    void rechazaVacio() {
        assertThrows(ValorFueraDeDominioException.class, () -> ValorNumerico.desdeTexto("   ", "valor"));
        assertThrows(ValorFueraDeDominioException.class, () -> ValorNumerico.desdeTexto(null, "valor"));
    }

    @Test
    @DisplayName("Un número que desborda double se rechaza")
    void rechazaDesbordamiento() {
        String enorme = "9".repeat(400);
        assertThrows(ValorFueraDeDominioException.class, () -> ValorNumerico.desdeTexto(enorme, "valor"));
    }
}
