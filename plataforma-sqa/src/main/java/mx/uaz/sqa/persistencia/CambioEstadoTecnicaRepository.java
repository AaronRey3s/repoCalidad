package mx.uaz.sqa.persistencia;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.CambioEstadoTecnica;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de la bitácora inmutable de cambios de estado de técnicas (RNF-03).
 */
public interface CambioEstadoTecnicaRepository extends JpaRepository<CambioEstadoTecnica, Long> {

    List<CambioEstadoTecnica> findByTecnicaIdOrderByRegistradoEnAscIdAsc(Long tecnicaId);
}
