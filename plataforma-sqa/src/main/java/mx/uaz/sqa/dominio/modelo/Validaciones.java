package mx.uaz.sqa.dominio.modelo;

import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;

/**
 * Validaciones de texto reutilizadas por las entidades del dominio.
 * Las entidades se protegen a sí mismas: no dependen de que la vista haya validado.
 */
public final class Validaciones {

    private Validaciones() {
    }

    /** Devuelve el texto sin espacios laterales o lanza excepción si está vacío o es demasiado largo. */
    public static String requerido(String valor, String mensaje, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(mensaje, campo);
        }
        String limpio = valor.trim();
        validarLongitud(limpio, campo, longitudMaxima);
        return limpio;
    }

    /** Devuelve el texto sin espacios laterales, o {@code null} si viene vacío. */
    public static String opcional(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        validarLongitud(limpio, campo, longitudMaxima);
        return limpio;
    }

    private static void validarLongitud(String valor, String campo, int longitudMaxima) {
        if (valor.length() > longitudMaxima) {
            throw new DatosInvalidosException(
                    "El campo admite como máximo " + longitudMaxima + " caracteres", campo);
        }
    }
}
