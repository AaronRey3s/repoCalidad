package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nombre del archivo del reporte PDF")
class NombreArchivoReporteTest {

    @Test
    @DisplayName("El slug elimina acentos y caracteres especiales")
    void slug() {
        assertEquals("evaluacion-n-1", ReporteService.slug("Evaluación Ñ #1"));
        assertEquals("sin-nombre", ReporteService.slug("  "));
        assertEquals("sin-nombre", ReporteService.slug(null));
    }
}
