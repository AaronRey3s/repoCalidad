package mx.uaz.sqa.dominio.modelo;

import java.util.regex.Pattern;
import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;

/**
 * Convierte el texto capturado por el usuario en un valor numérico (CU03 flujo 2a:
 * "si el actor introduce caracteres no numéricos ... el sistema impide el registro").
 * Acepta punto o coma decimal. Rechaza notación científica, NaN e Infinity.
 */
public final class ValorNumerico {

    private static final Pattern FORMATO = Pattern.compile("^[-+]?\\d+([.,]\\d+)?$");

    private ValorNumerico() {
    }

    public static double desdeTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ValorFueraDeDominioException("Ingresa el valor numérico obtenido en la medición", campo);
        }
        String limpio = texto.trim();
        if (!FORMATO.matcher(limpio).matches()) {
            throw new ValorFueraDeDominioException("El valor \"" + limpio
                    + "\" no es numérico. Usa solo dígitos y punto decimal (p. ej. 85.5)", campo);
        }
        double valor = Double.parseDouble(limpio.replace(',', '.'));
        if (!Double.isFinite(valor)) {
            throw new ValorFueraDeDominioException("El valor es demasiado grande", campo);
        }
        return valor;
    }
}
