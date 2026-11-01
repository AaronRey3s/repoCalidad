package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.PeriodoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Periodo de evaluación (RS-01, CU01 flujo 3a)")
class PeriodoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 8, 3);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 11);

    @Test
    @DisplayName("Dado un rango válido, cuando se crea el periodo, entonces conserva etiqueta y fechas")
    void creaPeriodoValido() {
        Periodo periodo = Periodo.de("  Ago-Dic 2026  ", INICIO, FIN);

        assertEquals("Ago-Dic 2026", periodo.getEtiqueta());
        assertEquals(INICIO, periodo.getFechaInicio());
        assertEquals(FIN, periodo.getFechaFin());
        assertEquals("Ago-Dic 2026 (03/08/2026 – 11/12/2026)", periodo.descripcion());
    }

    @Test
    @DisplayName("Dada una fecha de fin anterior al inicio, entonces se rechaza y se señala el campo fechaFin")
    void rechazaFinAnteriorAInicio() {
        PeriodoInvalidoException error = assertThrows(PeriodoInvalidoException.class,
                () -> Periodo.de("Ago-Dic 2026", FIN, INICIO));

        assertEquals("fechaFin", error.getCampo());
        assertTrue(error.getMessage().contains("no puede ser anterior"));
    }

    @Test
    @DisplayName("Un periodo de un solo día (inicio = fin) es válido (valor límite)")
    void aceptaPeriodoDeUnDia() {
        Periodo periodo = Periodo.de("Taller", INICIO, INICIO);

        assertTrue(periodo.contiene(INICIO));
    }

    @Test
    @DisplayName("Faltan datos obligatorios: etiqueta, inicio o fin")
    void rechazaDatosIncompletos() {
        assertEquals("etiquetaPeriodo",
                assertThrows(DatosInvalidosException.class, () -> Periodo.de(" ", INICIO, FIN)).getCampo());
        assertEquals("fechaInicio",
                assertThrows(DatosInvalidosException.class, () -> Periodo.de("P", null, FIN)).getCampo());
        assertEquals("fechaFin",
                assertThrows(DatosInvalidosException.class, () -> Periodo.de("P", INICIO, null)).getCampo());
    }

    @Test
    @DisplayName("contiene() respeta los límites del periodo")
    void contieneFechasDelRango() {
        Periodo periodo = Periodo.de("P", INICIO, FIN);

        assertTrue(periodo.contiene(INICIO));
        assertTrue(periodo.contiene(FIN));
        assertFalse(periodo.contiene(INICIO.minusDays(1)));
        assertFalse(periodo.contiene(FIN.plusDays(1)));
        assertFalse(periodo.contiene(null));
    }
}
