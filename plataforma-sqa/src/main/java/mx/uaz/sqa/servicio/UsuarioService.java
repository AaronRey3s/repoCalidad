package mx.uaz.sqa.servicio;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.modelo.Usuario;
import mx.uaz.sqa.persistencia.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de usuarios (responsables de técnicas y datos del usuario en sesión).
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<Usuario> listar() {
        return usuarios.findAllByOrderByNombreCompletoAsc();
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return usuarios.findByUsername(username);
    }
}
