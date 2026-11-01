package mx.uaz.sqa.dominio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;

/**
 * Unidad de medida de una métrica (CU02 paso 2: porcentaje, conteo o razón).
 * Cada unidad define polimórficamente qué valores admite (CU03 flujo 2a).
 */
public enum UnidadMedida {

    PORCENTAJE("Porcentaje (%)", "%") {
        @Override
        public boolean admite(double valor) {
            return Double.isFinite(valor) && valor >= 0 && valor <= 100;
        }

        @Override
        public String reglaDeDominio() {
            return "un porcentaje debe estar en el rango [0, 100]";
        }
    },

    CONTEO("Conteo", "") {
        @Override
        public boolean admite(double valor) {
            return Double.isFinite(valor) && valor >= 0 && valor == Math.rint(valor);
        }

        @Override
        public String reglaDeDominio() {
            return "un conteo debe ser un número entero mayor o igual a 0";
        }
    },

    RAZON("Razón", "") {
        @Override
        public boolean admite(double valor) {
            return Double.isFinite(valor) && valor >= 0;
        }

        @Override
        public String reglaDeDominio() {
            return "una razón debe ser un número mayor o igual a 0";
        }
    };

    private final String etiqueta;
    private final String simbolo;

    UnidadMedida(String etiqueta, String simbolo) {
        this.etiqueta = etiqueta;
        this.simbolo = simbolo;
    }

    /** Indica si el valor pertenece al dominio válido de la unidad. */
    public abstract boolean admite(double valor);

    /** Descripción legible de la regla de dominio de la unidad. */
    public abstract String reglaDeDominio();

    /**
     * Lanza {@link ValorFueraDeDominioException} si el valor no es admitido.
     *
     * @param campo campo del formulario a resaltar en caso de error
     */
    public void validar(double valor, String campo) {
        if (!admite(valor)) {
            String mostrado = Double.isFinite(valor) ? formatear(valor) : String.valueOf(valor);
            throw new ValorFueraDeDominioException(
                    "El valor " + mostrado + " no es válido: " + reglaDeDominio() + ".", campo);
        }
    }

    /** Formatea el valor con máximo 2 decimales y el símbolo de la unidad. */
    public String formatear(double valor) {
        String numero = BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
        return simbolo.isEmpty() ? numero : numero + " " + simbolo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
