package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.ReporteSinDatosException;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.Evidencia;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import mx.uaz.sqa.servicio.dto.FilaMetricaReporte;
import mx.uaz.sqa.servicio.dto.ReporteSalud;
import mx.uaz.sqa.servicio.reporte.GeneradorReporte;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU06 – Exportar reporte de salud (servicio)")
class ReporteServiceTest {

    @Mock
    private ProyectoRepository proyectos;
    @Mock
    private MetricaRepository metricas;
    @Mock
    private MedicionRepository mediciones;
    @Mock
    private TecnicaSqaRepository tecnicas;
    @Mock
    private GeneradorReporte generador;

    private ReporteService servicio;
    private Proyecto proyecto;
    private Metrica cobertura;

    @BeforeEach
    void preparar() {
        servicio = new ReporteService(new LectorSaludProyecto(proyectos, metricas, mediciones, tecnicas),
                generador, Fabrica.reloj());
        proyecto = Fabrica.conId(Fabrica.proyecto(), 1L);
        cobertura = Fabrica.conId(Fabrica.metricaCobertura(proyecto), 10L);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(metricas.findByProyectoIdOrderByNombreAsc(1L)).thenReturn(List.of(cobertura));
    }

    private void conDatos() {
        Medicion medicion = cobertura.registrarMedicion(55, null, "dev", Fabrica.AHORA.minusDays(1));
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);
        tecnica.cambiarEstado(EstadoTecnica.APLICADO, Evidencia.deUrl("https://github.com/x/pull/2"), "sqa", Fabrica.AHORA);
        when(mediciones.findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(1L)).thenReturn(List.of(medicion));
        when(tecnicas.findByProyectoIdOrderByIdAsc(1L)).thenReturn(List.of(tecnica));
    }

    @Test
    @DisplayName("RS-10: consolida datos generales, métricas con histórico, técnicas con evidencia e índice global")
    void consolidaReporte() {
        conDatos();

        ReporteSalud reporte = servicio.consolidar(1L, "sqa");

        assertEquals("sqa", reporte.emitidoPor());
        assertEquals(Fabrica.AHORA, reporte.emitidoEn());
        assertEquals("Plataforma SQA", reporte.proyecto().nombre());
        FilaMetricaReporte fila = reporte.metricas().get(0);
        assertEquals(EstadoSemaforo.ROJO, fila.estado());
        assertEquals("55 %", fila.valorActual());
        assertEquals(1, fila.historial().size());
        assertEquals("dev", fila.historial().get(0).autor());
        assertTrue(reporte.tecnicas().get(0).evidencia().startsWith("URL:"));
        // Métricas: Rojo (0) ; técnicas 100 % → 50
        assertEquals(50.0, reporte.salud().indiceGlobal());
    }

    @Test
    @DisplayName("CU06 flujo 2a: sin registros en el periodo → se notifica y se cancela")
    void sinDatos() {
        when(mediciones.findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(1L)).thenReturn(List.of());
        when(tecnicas.findByProyectoIdOrderByIdAsc(1L)).thenReturn(List.of());

        assertThrows(ReporteSinDatosException.class, () -> servicio.exportarPdf(1L, "sqa"));
        verifyNoInteractions(generador);
    }

    @Test
    @DisplayName("RS-11: exporta el PDF con un nombre de archivo descriptivo")
    void exportaPdf() {
        conDatos();
        byte[] pdf = {37, 80, 68, 70};
        when(generador.generar(any(ReporteSalud.class))).thenReturn(pdf);

        ArchivoDescargable archivo = servicio.exportarPdf(1L, "sqa");

        assertEquals("reporte-salud-plataforma-sqa-ago-dic-2026.pdf", archivo.nombre());
        assertArrayEquals(pdf, archivo.contenido());
    }
}
