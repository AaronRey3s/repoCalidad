package mx.uaz.sqa.dominio.modelo;

/**
 * Estado de salud de una métrica (RS-05). RNF-02: codificación cromática estándar
 * verde (Óptimo), amarillo (Advertencia) y rojo (Crítico).
 */
public enum EstadoSemaforo {

    VERDE("Óptimo", "verde", "#1E8E3E", 100),
    AMARILLO("Advertencia", "amarillo", "#F9AB00", 50),
    ROJO("Crítico", "rojo", "#D93025", 0),
    SIN_DATOS("Sin datos", "sin-datos", "#9AA0A6", 0);

    private final String etiqueta;
    private final String claseCss;
    private final String colorHex;
    private final int puntaje;

    EstadoSemaforo(String etiqueta, String claseCss, String colorHex, int puntaje) {
        this.etiqueta = etiqueta;
        this.claseCss = claseCss;
        this.colorHex = colorHex;
        this.puntaje = puntaje;
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

    /** Aporte al índice de salud: Verde 100, Amarillo 50, Rojo 0. */
    public int getPuntaje() {
        return puntaje;
    }

    public boolean tieneDatos() {
        return this != SIN_DATOS;
    }
}
