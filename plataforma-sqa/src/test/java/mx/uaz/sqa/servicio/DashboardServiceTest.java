package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.Dictamen;
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
import mx.uaz.sqa.servicio.dto.DashboardProyecto;
import mx.uaz.sqa.servicio.dto.IndicadorMetrica;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU05 – Dashboard de salud SQA (servicio)")
class DashboardServiceTest {

    @Mock
    private ProyectoRepository proyectos;
    @Mock
    private MetricaRepository metricas;
    @Mock
    private MedicionRepository mediciones;
    @Mock
    private TecnicaSqaRepository tecnicas;

    private DashboardService servicio;
    private Proyecto proyecto;
    private Metrica cobertura;
    private Metrica defectos;

    @BeforeEach
    void preparar() {
        servicio = new DashboardService(new LectorSaludProyecto(proyectos, metricas, mediciones, tecnicas));
        proyecto = Fabrica.conId(Fabrica.proyecto(), 1L);
        cobertura = Fabrica.conId(Fabrica.metricaCobertura(proyecto), 10L);
        defectos = Fabrica.conId(Fabrica.metricaDefectos(proyecto), 11L);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(metricas.findByProyectoIdOrderByNombreAsc(1L)).thenReturn(List.of(cobertura, defectos));
    }

    @Test
    @DisplayName("Dado un proyecto con mediciones y técnicas, entonces muestra semáforos, tendencia y cobertura de técnicas")
    void construyeDashboard() {
        Medicion primera = cobertura.registrarMedicion(70, null, "dev", Fabrica.AHORA.minusDays(7));
        Medicion segunda = cobertura.registrarMedicion(85, null, "dev", Fabrica.AHORA);
        TecnicaSqa aplicada = Fabrica.tecnica(proyecto);
        aplicada.cambiarEstado(EstadoTecnica.APLICADO, Evidencia.deUrl("https://github.com/x"), "sqa", Fabrica.AHORA);
        TecnicaSqa pendiente = Fabrica.tecnica(proyecto);
        when(mediciones.findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(1L)).thenReturn(List.of(primera, segunda));
        when(tecnicas.findByProyectoIdOrderByIdAsc(1L)).thenReturn(List.of(aplicada, pendiente));

        DashboardProyecto dashboard = servicio.construir(1L);

        IndicadorMetrica indicadorCobertura = dashboard.metricas().get(0);
        assertEquals(EstadoSemaforo.VERDE, indicadorCobertura.estado());
        assertEquals(2, indicadorCobertura.serie().size());
        assertEquals("85 %", indicadorCobertura.valorActual());
        assertTrue(indicadorCobertura.esPorcentaje());
        assertEquals(10L, indicadorCobertura.id());
        assertFalse(dashboard.metricas().get(1).tieneDatos());
        assertEquals(1, dashboard.tecnicas().aplicadas());
        assertEquals(1, dashboard.tecnicas().pendientes());
        assertEquals(50.0, dashboard.tecnicas().porcentajeAplicadas());
        assertEquals("50 %", dashboard.tecnicas().porcentajeTexto());
        assertEquals(50, dashboard.tecnicas().anchoBarra());
        // Métricas: Verde (100) ; técnicas 50 % → índice global 75 → En riesgo
        assertEquals(75.0, dashboard.salud().indiceGlobal());
        assertEquals(Dictamen.EN_RIESGO, dashboard.salud().dictamen());
        assertFalse(dashboard.sinMediciones());
        assertEquals(1L, dashboard.proyecto().id());
    }

    @Test
    @DisplayName("CU05 flujo 2a: sin mediciones el panel muestra indicadores en 'Sin datos'")
    void sinMediciones() {
        when(mediciones.findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(1L)).thenReturn(List.of());
        when(tecnicas.findByProyectoIdOrderByIdAsc(1L)).thenReturn(List.of());

        DashboardProyecto dashboard = servicio.construir(1L);

        assertTrue(dashboard.sinMediciones());
        assertEquals(EstadoSemaforo.SIN_DATOS, dashboard.metricas().get(0).estado());
        assertEquals(Dictamen.SIN_DATOS, dashboard.salud().dictamen());
        assertNull(dashboard.tecnicas().porcentajeAplicadas());
        assertEquals("Sin técnicas", dashboard.tecnicas().porcentajeTexto());
        assertEquals(0, dashboard.tecnicas().anchoBarra());
    }
}
