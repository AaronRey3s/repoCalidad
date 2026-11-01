package mx.uaz.sqa.persistencia;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.Metrica;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de métricas configuradas (RS-03).
 */
public interface MetricaRepository extends JpaRepository<Metrica, Long> {

    List<Metrica> findByProyectoIdOrderByNombreAsc(Long proyectoId);

    boolean existsByProyectoIdAndNombreIgnoreCase(Long proyectoId, String nombre);
}
