package mx.uaz.sqa.configuracion.seguridad;

import mx.uaz.sqa.dominio.modelo.Usuario;
import mx.uaz.sqa.persistencia.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga los usuarios desde la base de datos para Spring Security.
 * El rol del dominio se traduce a la autoridad ROLE_&lt;ROL&gt;.
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetallesService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarios.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}
