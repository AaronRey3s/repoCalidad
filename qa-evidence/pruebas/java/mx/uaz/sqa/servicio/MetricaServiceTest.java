package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.MetricaDuplicadaException;
import mx.uaz.sqa.dominio.excepcion.OperacionNoPermitidaException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.excepcion.UmbralesInvalidosException;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.servicio.dto.DatosMetrica;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU02 – Configurar métricas de calidad (servicio)")
class MetricaServiceTest {

    @Mock
    private ProyectoRepository proyectos;
    @Mock
    private MetricaRepository metricas;
    @Mock
    private MedicionRepository mediciones;

    private MetricaService servicio;
    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        servicio = new MetricaService(proyectos, metricas, mediciones);
        proyecto = Fabrica.conId(Fabrica.proyecto(), 1L);
    }

    private static DatosMetrica cobertura(double optimo, double critico) {
        return new DatosMetrica("Cobertura", UnidadMedida.PORCENTAJE, Frecuencia.POR_SPRINT,
                SentidoMetrica.MAYOR_ES_MEJOR, optimo, critico);
    }

    @Test
    @DisplayName("Dada una configuración válida, entonces la métrica queda lista para recibir mediciones")
    void configuraMetrica() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(metricas.existsByProyectoIdAndNombreIgnoreCase(1L, "Cobertura")).thenReturn(false);
        when(metricas.save(any(Metrica.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Metrica metrica = servicio.configurar(1L, cobertura(80, 60));

        assertEquals("Cobertura", metrica.getNombre());
        assertEquals(EstadoSemaforo.SIN_DATOS, metrica.getEstadoActual());
        assertEquals("≥ 80 %", metrica.rangoVerde());
        verify(metricas).save(metrica);
    }

    @Test
    @DisplayName("CU02 flujo 3a: umbrales cruzados → se rechaza el registro")
    void rechazaUmbralesCruzados() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));

        assertThrows(UmbralesInvalidosException.class, () -> servicio.configurar(1L, cobertura(60, 80)));

        verify(metricas, never()).save(any());
    }

    @Test
    @DisplayName("No se permiten dos métricas con el mismo nombre en un proyecto")
    void rechazaDuplicada() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(metricas.existsByProyectoIdAndNombreIgnoreCase(1L, "Cobertura")).thenReturn(true);

        assertEquals("nombre", assertThrows(MetricaDuplicadaException.class,
                () -> servicio.configurar(1L, cobertura(80, 60))).getCampo());
    }

    @Test
    @DisplayName("Precondición: el proyecto debe estar activo")
    void rechazaProyectoInactivo() {
        proyecto.desactivar();
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));

        assertThrows(ProyectoInactivoException.class, () -> servicio.configurar(1L, cobertura(80, 60)));
        verifyNoInteractions(metricas);
    }

    @Test
    @DisplayName("Proyecto inexistente")
    void proyectoInexistente() {
        when(proyectos.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.configurar(5L, cobertura(80, 60)));
    }

    @Test
    @DisplayName("Se puede eliminar una métrica sin mediciones")
    void eliminaSinMediciones() {
        Metrica metrica = Fabrica.conId(Fabrica.metricaCobertura(proyecto), 9L);
        when(metricas.findById(9L)).thenReturn(Optional.of(metrica));
        when(mediciones.countByMetricaId(9L)).thenReturn(0L);

        Long proyectoId = servicio.eliminar(9L);

        assertEquals(1L, proyectoId);
        verify(metricas).delete(metrica);
    }

    @Test
    @DisplayName("RNF-03: una métrica con mediciones no se puede eliminar")
    void noEliminaConMediciones() {
        Metrica metrica = Fabrica.conId(Fabrica.metricaCobertura(proyecto), 9L);
        when(metricas.findById(9L)).thenReturn(Optional.of(metrica));
        when(mediciones.countByMetricaId(9L)).thenReturn(3L);

        assertThrows(OperacionNoPermitidaException.class, () -> servicio.eliminar(9L));
        verify(metricas, never()).delete(any());
    }

    @Test
    @DisplayName("Consulta de métricas del proyecto y de una métrica inexistente")
    void consultas() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);
        when(metricas.findByProyectoIdOrderByNombreAsc(1L)).thenReturn(List.of(metrica));
        when(metricas.findById(77L)).thenReturn(Optional.empty());

        assertEquals(1, servicio.listarPorProyecto(1L).size());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(77L));
    }
}
