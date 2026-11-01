package mx.uaz.sqa.servicio;

import java.util.List;
import mx.uaz.sqa.dominio.excepcion.MetricaDuplicadaException;
import mx.uaz.sqa.dominio.excepcion.OperacionNoPermitidaException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.servicio.dto.DatosMetrica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU02 – Configurar Métricas de Calidad del Proyecto (RS-03).
 */
@Service
@Transactional
public class MetricaService {

    private final ProyectoRepository proyectos;
    private final MetricaRepository metricas;
    private final MedicionRepository mediciones;

    public MetricaService(ProyectoRepository proyectos, MetricaRepository metricas, MedicionRepository mediciones) {
        this.proyectos = proyectos;
        this.metricas = metricas;
        this.mediciones = mediciones;
    }

    @Transactional(readOnly = true)
    public List<Metrica> listarPorProyecto(Long proyectoId) {
        return metricas.findByProyectoIdOrderByNombreAsc(proyectoId);
    }

    @Transactional(readOnly = true)
    public Metrica obtener(Long metricaId) {
        return metricas.findById(metricaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la métrica", metricaId));
    }

    /** RS-03: registra la métrica con unidad, periodicidad y umbrales Verde/Amarillo/Rojo. */
    public Metrica configurar(Long proyectoId, DatosMetrica datos) {
        Proyecto proyecto = proyectos.findById(proyectoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el proyecto", proyectoId));
        proyecto.verificarActivo();
        Metrica metrica = new Metrica(proyecto, datos.nombre(), datos.unidad(), datos.frecuencia(),
                datos.sentido(), datos.limiteOptimo(), datos.limiteCritico());
        if (metricas.existsByProyectoIdAndNombreIgnoreCase(proyectoId, metrica.getNombre())) {
            throw new MetricaDuplicadaException("El proyecto ya tiene una métrica llamada \""
                    + metrica.getNombre() + "\"", "nombre");
        }
        return metricas.save(metrica);
    }

    /**
     * Elimina una métrica capturada por error. Solo se permite si no tiene mediciones,
     * porque el histórico es inmutable (RNF-03).
     *
     * @return id del proyecto al que pertenecía la métrica
     */
    public Long eliminar(Long metricaId) {
        Metrica metrica = obtener(metricaId);
        if (mediciones.countByMetricaId(metricaId) > 0) {
            throw new OperacionNoPermitidaException("No se puede eliminar \"" + metrica.getNombre()
                    + "\": ya tiene mediciones registradas y el histórico es inmutable (RNF-03)");
        }
        Long proyectoId = metrica.getProyecto().getId();
        metricas.delete(metrica);
        return proyectoId;
    }
}
