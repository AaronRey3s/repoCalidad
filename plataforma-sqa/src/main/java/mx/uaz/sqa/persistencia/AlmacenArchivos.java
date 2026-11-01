package mx.uaz.sqa.persistencia;

/**
 * Abstracción del almacenamiento de archivos de evidencia (RU-05, RS-08).
 * La capa de servicio depende de esta interfaz y no del sistema de archivos (principio DIP).
 */
public interface AlmacenArchivos {

    /**
     * Guarda el contenido y devuelve la clave única con la que se podrá recuperar.
     */
    String guardar(String nombreOriginal, byte[] contenido);

    byte[] leer(String clave);

    void eliminar(String clave);
}
