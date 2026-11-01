package mx.uaz.sqa.persistencia;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia del histórico inmutable de mediciones (RS-04, RNF-03).
 */
public interface MedicionRepository extends JpaRepository<Medicion, Long> {

    List<Medicion> findByMetricaIdOrderByRegistradoEnAscIdAsc(Long metricaId);

    /** Todas las mediciones de un proyecto en orden cronológico (dashboard y reporte). */
    List<Medicion> findByMetricaProyectoIdOrderByRegistradoEnAscIdAsc(Long proyectoId);

    long countByMetricaId(Long metricaId);
}
