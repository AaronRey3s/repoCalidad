package mx.uaz.sqa.servicio.reporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import mx.uaz.sqa.dominio.modelo.Dictamen;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.IndiceSalud;
import mx.uaz.sqa.servicio.dto.FilaMedicionReporte;
import mx.uaz.sqa.servicio.dto.FilaMetricaReporte;
import mx.uaz.sqa.servicio.dto.FilaTecnicaReporte;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import mx.uaz.sqa.servicio.dto.ReporteSalud;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RS-11 – Generación del reporte en PDF")
class GeneradorReportePdfTest {

    private final GeneradorReportePdf generador = new GeneradorReportePdf();

    private static ProyectoResumen proyecto() {
        return new ProyectoResumen(1L, "Plataforma SQA", "Descripción", "Java", "Ago-Dic 2026",
                Fabrica.INICIO_PERIODO, Fabrica.FIN_PERIODO, "Ago-Dic 2026 (03/08/2026 – 11/12/2026)", true);
    }

    @Test
    @DisplayName("Genera un documento PDF válido con métricas, técnicas e histórico")
    void generaPdfCompleto() {
        FilaMetricaReporte metrica = new FilaMetricaReporte("Cobertura", "Porcentaje (%)", "≥ 80 %",
                "≥ 60 % y < 80 %", "< 60 %", "85 %", EstadoSemaforo.VERDE,
                List.of(new FilaMedicionReporte(Fabrica.AHORA, "85 %", EstadoSemaforo.VERDE, "dev")));
        FilaMetricaReporte sinHistorial = new FilaMetricaReporte("Defectos", "Razón", "≤ 1", "> 1 y ≤ 3", "> 3",
                "—", EstadoSemaforo.SIN_DATOS, List.of());
        FilaTecnicaReporte tecnica = new FilaTecnicaReporte("Revisión por pares", "Revisión Técnica / Pares",
                EstadoTecnica.APLICADO, "sqa", "URL: https://github.com/x");
        ReporteSalud reporte = new ReporteSalud(proyecto(), Fabrica.AHORA, "sqa", List.of(metrica, sinHistorial),
                List.of(tecnica), new IndiceSalud(100.0, 100.0, 100.0, Dictamen.SALUDABLE));

        byte[] pdf = generador.generar(reporte);

        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
    }

    @Test
    @DisplayName("Genera el PDF aunque no haya métricas ni técnicas")
    void generaPdfVacio() {
        ReporteSalud reporte = new ReporteSalud(proyecto(), Fabrica.AHORA, "sqa", List.of(), List.of(),
                new IndiceSalud(null, null, null, Dictamen.SIN_DATOS));

        byte[] pdf = generador.generar(reporte);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
    }

    @Test
    @DisplayName("Reemplaza ≥ y ≤ por símbolos compatibles con las fuentes estándar del PDF")
    void textoCompatible() {
        assertEquals(">= 80 % y <= 90 %", GeneradorReportePdf.compatibleConPdf("≥ 80 % y ≤ 90 %"));
        assertEquals("", GeneradorReportePdf.compatibleConPdf(null));
    }
}
