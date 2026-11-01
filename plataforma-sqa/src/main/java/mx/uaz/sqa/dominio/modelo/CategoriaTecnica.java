package mx.uaz.sqa.dominio.modelo;

/**
 * Categorías de técnicas de aseguramiento de calidad (RS-06).
 */
public enum CategoriaTecnica {

    REVISION_TECNICA_PARES("Revisión Técnica / Pares"),
    PRUEBAS_SOFTWARE("Pruebas de Software"),
    ANALISIS_ESTATICO("Análisis Estático"),
    AUDITORIA_PROCESO("Auditoría de Proceso");

    private final String etiqueta;

    CategoriaTecnica(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
