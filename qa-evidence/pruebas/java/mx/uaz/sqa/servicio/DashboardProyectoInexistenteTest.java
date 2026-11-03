package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU05 – proyecto inexistente")
class DashboardProyectoInexistenteTest {

    @Mock
    private ProyectoRepository proyectos;
    @Mock
    private MetricaRepository metricas;
    @Mock
    private MedicionRepository mediciones;
    @Mock
    private TecnicaSqaRepository tecnicas;

    @Test
    @DisplayName("Solicitar el dashboard de un proyecto inexistente produce 404")
    void proyectoInexistente() {
        DashboardService servicio = new DashboardService(new LectorSaludProyecto(proyectos, metricas, mediciones, tecnicas));
        when(proyectos.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.construir(404L));
    }
}
