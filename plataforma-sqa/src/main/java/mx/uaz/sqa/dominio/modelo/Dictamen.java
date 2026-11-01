package mx.uaz.sqa.dominio.modelo;

/**
 * Dictamen global de salud del software (CU06 paso 2).
 */
public enum Dictamen {

    SALUDABLE("Saludable", "verde", "#1E8E3E"),
    EN_RIESGO("En riesgo", "amarillo", "#F9AB00"),
    CRITICO("Crítico", "rojo", "#D93025"),
    SIN_DATOS("Sin datos", "sin-datos", "#9AA0A6");

    private final String etiqueta;
    private final String claseCss;
    private final String colorHex;

    Dictamen(String etiqueta, String claseCss, String colorHex) {
        this.etiqueta = etiqueta;
        this.claseCss = claseCss;
        this.colorHex = colorHex;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getClaseCss() {
        return claseCss;
    }

    public String getColorHex() {
        return colorHex;
    }
}
