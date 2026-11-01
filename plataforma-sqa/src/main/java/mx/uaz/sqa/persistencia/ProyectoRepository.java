package mx.uaz.sqa.persistencia;

import mx.uaz.sqa.dominio.modelo.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de proyectos (RS-01, RS-02).
 */
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /** CU01 paso 3: ¿existe otro proyecto con el mismo nombre en el mismo periodo? */
    boolean existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCase(String nombre, String etiquetaPeriodo);

    /** Igual que el anterior pero excluyendo al propio proyecto (edición, RS-02). */
    boolean existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCaseAndIdNot(String nombre, String etiquetaPeriodo, Long id);
}
