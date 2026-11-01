package mx.uaz.sqa.servicio.dto;

/**
 * Archivo recibido desde la vista, independiente de la API web (MultipartFile).
 */
public record ArchivoAdjunto(String nombreOriginal, byte[] contenido) {

    public boolean estaVacio() {
        return contenido == null || contenido.length == 0;
    }
}
