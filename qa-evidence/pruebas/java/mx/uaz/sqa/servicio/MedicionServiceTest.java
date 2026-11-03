package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU03 – Registrar medición de métrica (servicio)")
class MedicionServiceTest {

    @Mock
    private MetricaRepository metricas;
    @Mock
    private MedicionRepository mediciones;

    private MedicionService servicio;
    private Metrica metrica;

    @BeforeEach
    void preparar() {
        servicio = new MedicionService(metricas, mediciones, Fabrica.reloj());
        metrica = Fabrica.conId(Fabrica.metricaCobertura(Fabrica.proyecto()), 4L);
    }

    @Test
    @DisplayName("Dado 72.5 %, cuando se registra, entonces queda en Amarillo con fecha/hora y autor (RS-04, RS-05, RNF-03)")
    void registraMedicion() {
        when(metricas.findById(4L)).thenReturn(Optional.of(metrica));
        when(mediciones.save(any(Medicion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Medicion medicion = servicio.registrar(4L, "72.5", "Sprint 1", "dev");

        assertEquals(EstadoSemaforo.AMARILLO, medicion.getEstado());
        assertEquals(72.5, medicion.getValor());
        assertEquals("dev", medicion.getAutor());
        assertEquals(Fabrica.AHORA, medicion.getRegistradoEn());
        assertEquals(EstadoSemaforo.AMARILLO, metrica.getEstadoActual());
        verify(mediciones).save(medicion);
    }

    @Test
    @DisplayName("CU03 flujo 2a: caracteres no numéricos → se impide el registro")
    void rechazaTextoNoNumerico() {
        when(metricas.findById(4L)).thenReturn(Optional.of(metrica));

        ValorFueraDeDominioException error = assertThrows(ValorFueraDeDominioException.class,
                () -> servicio.registrar(4L, "ochenta", null, "dev"));

        assertEquals("valor", error.getCampo());
        assertEquals(EstadoSemaforo.SIN_DATOS, metrica.getEstadoActual());
        verify(mediciones, never()).save(any());
    }

    @Test
    @DisplayName("CU03 flujo 2a: porcentaje fuera de [0, 100] → se impide el registro")
    void rechazaPorcentajeFueraDeRango() {
        when(metricas.findById(4L)).thenReturn(Optional.of(metrica));

        assertThrows(ValorFueraDeDominioException.class, () -> servicio.registrar(4L, "150", null, "dev"));
        verify(mediciones, never()).save(any());
    }

    @Test
    @DisplayName("Métrica inexistente")
    void metricaInexistente() {
        when(metricas.findById(8L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(8L, "50", null, "dev"));
    }

    @Test
    @DisplayName("El historial se obtiene en orden cronológico desde el repositorio")
    void historial() {
        when(mediciones.findByMetricaIdOrderByRegistradoEnAscIdAsc(4L)).thenReturn(List.of());

        assertTrue(servicio.historial(4L).isEmpty());
    }
}
