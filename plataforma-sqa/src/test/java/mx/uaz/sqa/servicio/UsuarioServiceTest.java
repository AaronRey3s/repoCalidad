package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.modelo.Rol;
import mx.uaz.sqa.dominio.modelo.Usuario;
import mx.uaz.sqa.persistencia.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Consulta de usuarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    @Test
    @DisplayName("Lista usuarios y busca por nombre de usuario")
    void consultas() {
        Usuario dev = new Usuario("dev", "Desarrollador", "hash", Rol.DESARROLLADOR);
        when(usuarios.findAllByOrderByNombreCompletoAsc()).thenReturn(List.of(dev));
        when(usuarios.findByUsername("dev")).thenReturn(Optional.of(dev));
        UsuarioService servicio = new UsuarioService(usuarios);

        assertEquals(1, servicio.listar().size());
        assertEquals("Desarrollador", servicio.buscarPorUsername("dev").orElseThrow().getNombreCompleto());
    }

    @Test
    @DisplayName("Un nombre vacío no consulta la base de datos")
    void nombreVacio() {
        UsuarioService servicio = new UsuarioService(usuarios);

        assertTrue(servicio.buscarPorUsername(" ").isEmpty());
        assertTrue(servicio.buscarPorUsername(null).isEmpty());
        verifyNoInteractions(usuarios);
    }
}
