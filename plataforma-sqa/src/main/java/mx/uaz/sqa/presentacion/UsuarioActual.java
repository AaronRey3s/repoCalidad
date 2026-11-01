package mx.uaz.sqa.presentacion;

/**
 * Datos del usuario en sesión que se muestran en la barra de navegación.
 */
public record UsuarioActual(String username, String nombre, String rol) {
}
