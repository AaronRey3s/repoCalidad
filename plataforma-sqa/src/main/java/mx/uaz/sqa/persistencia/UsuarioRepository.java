package mx.uaz.sqa.persistencia;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de usuarios del sistema.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findAllByOrderByNombreCompletoAsc();
}
