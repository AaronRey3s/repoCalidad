package mx.uaz.sqa.dominio.modelo;

/**
 * Perfiles de actores del sistema (Tabla 4 del SRS).
 */
public enum Rol {

    LIDER_PROYECTO("Líder de Proyecto"),
    LIDER_SQA("Ingeniero / Líder SQA"),
    DESARROLLADOR("Desarrollador"),
    STAKEHOLDER("Stakeholder");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
