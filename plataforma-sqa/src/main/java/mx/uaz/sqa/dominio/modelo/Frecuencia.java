package mx.uaz.sqa.dominio.modelo;

/**
 * Frecuencia con la que se espera capturar una métrica (CU02 paso 2, RS-03 "periodicidad").
 */
public enum Frecuencia {

    DIARIA("Diaria"),
    SEMANAL("Semanal"),
    QUINCENAL("Quincenal"),
    MENSUAL("Mensual"),
    POR_SPRINT("Por sprint / iteración");

    private final String etiqueta;

    Frecuencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
