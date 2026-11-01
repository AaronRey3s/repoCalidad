package mx.uaz.sqa.servicio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.CalculadoraSalud;
import mx.uaz.sqa.dominio.modelo.IndiceSalud;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recupera el histórico de mediciones y el estado de las técnicas SQA de un proyecto
 * (CU05 paso 2) y calcula su índice de salud.
 */
@Component
public class LectorSaludProyecto {

    private final ProyectoRepository proyectos;
    private final MetricaRepository metricas;
    private final MedicionRepository mediciones;
    private final TecnicaSqaRepository tecnicas;
    private final CalculadoraSalud calculadora = new CalculadoraSalud();

    public LectorSaludProyecto(ProyectoRepository proyectos, MetricaRepository metricas,
                               MedicionRepository mediciones, TecnicaSqaRepository tecnicas) {
        this.proyectos = proyectos;
        this.metricas = metricas;
        this.mediciones = mediciones;
        this.tecnicas = tecnicas;
    }

    @Transactional(readOnly = true)
    public InstantaneaSalud leer(Long proyectoId) {
        Proyecto proyecto = proyectos.findById(proyectoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el proyecto", proyectoId));
        List<Metrica> metricasProyecto = metricas.findByProyectoIdOrderByNombreAsc(proyectoId);
        Map<Metrica, List<Medicion>> porMetrica = mediciones
                .findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(proyectoId).stream()
                .collect(Collectors.groupingBy(Medicion::getMetrica, LinkedHashMap::new, Collectors.toList()));
        List<TecnicaSqa> tecnicasProyecto = tecnicas.findByProyectoIdOrderByIdAsc(proyectoId);
        IndiceSalud salud = calculadora.calcular(
                metricasProyecto.stream().map(Metrica::getEstadoActual).toList(),
                tecnicasProyecto.stream().map(TecnicaSqa::getEstado).toList());
        return new InstantaneaSalud(proyecto, metricasProyecto, porMetrica, tecnicasProyecto, salud);
    }
}
