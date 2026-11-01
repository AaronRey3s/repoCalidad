package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dominio de valores por unidad de medida (CU03 flujo 2a)")
class UnidadMedidaTest {

    @Test
    @DisplayName("Porcentaje: admite [0, 100] y rechaza valores fuera del rango")
    void porcentaje() {
        assertTrue(UnidadMedida.PORCENTAJE.admite(0));
        assertTrue(UnidadMedida.PORCENTAJE.admite(100));
        assertFalse(UnidadMedida.PORCENTAJE.admite(-0.01));
        assertFalse(UnidadMedida.PORCENTAJE.admite(100.01));
        assertFalse(UnidadMedida.PORCENTAJE.admite(Double.NaN));
    }

    @Test
    @DisplayName("Conteo: solo enteros no negativos")
    void conteo() {
        assertTrue(UnidadMedida.CONTEO.admite(0));
        assertTrue(UnidadMedida.CONTEO.admite(15));
        assertFalse(UnidadMedida.CONTEO.admite(2.5));
        assertFalse(UnidadMedida.CONTEO.admite(-1));
    }

    @Test
    @DisplayName("Razón: cualquier real no negativo")
    void razon() {
        assertTrue(UnidadMedida.RAZON.admite(0.35));
        assertFalse(UnidadMedida.RAZON.admite(-0.1));
        assertFalse(UnidadMedida.RAZON.admite(Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("validar() lanza excepción con el campo a resaltar")
    void validarLanzaExcepcion() {
        ValorFueraDeDominioException error = assertThrows(ValorFueraDeDominioException.class,
                () -> UnidadMedida.PORCENTAJE.validar(150, "valor"));

        assertEquals("valor", error.getCampo());
        assertTrue(error.getMessage().contains("[0, 100]"));
        assertDoesNotThrow(() -> UnidadMedida.PORCENTAJE.validar(50, "valor"));
        assertThrows(ValorFueraDeDominioException.class, () -> UnidadMedida.RAZON.validar(Double.NaN, "valor"));
    }

    @Test
    @DisplayName("formatear() usa máximo 2 decimales y el símbolo de la unidad")
    void formatear() {
        assertEquals("85.5 %", UnidadMedida.PORCENTAJE.formatear(85.5));
        assertEquals("70 %", UnidadMedida.PORCENTAJE.formatear(70.0));
        assertEquals("3", UnidadMedida.CONTEO.formatear(3.0));
        assertEquals("1.33", UnidadMedida.RAZON.formatear(1.3333));
        assertEquals("0", UnidadMedida.RAZON.formatear(0.0));
    }

    @Test
    @DisplayName("Cada unidad tiene etiqueta y regla legible")
    void etiquetas() {
        for (UnidadMedida unidad : UnidadMedida.values()) {
            assertFalse(unidad.getEtiqueta().isBlank());
            assertFalse(unidad.reglaDeDominio().isBlank());
        }
        assertEquals("%", UnidadMedida.PORCENTAJE.getSimbolo());
    }
}
