package mx.uaz.sqa.persistencia;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de técnicas SQA asignadas (RS-06, RS-07).
 */
public interface TecnicaSqaRepository extends JpaRepository<TecnicaSqa, Long> {

    List<TecnicaSqa> findByProyectoIdOrderByIdAsc(Long proyectoId);
}
