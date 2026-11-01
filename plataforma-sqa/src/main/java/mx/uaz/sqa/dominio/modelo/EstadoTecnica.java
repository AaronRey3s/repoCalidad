package mx.uaz.sqa.dominio.modelo;

/**
 * Estado de ejecución de una técnica SQA asignada (RS-07).
 * Solo el estado {@link #APLICADO} exige evidencia (RS-08).
 */
public enum EstadoTecnica {

    PENDIENTE("Pendiente", "pendiente"),
    EN_PROGRESO("En progreso", "progreso"),
    APLICADO("Aplicado", "aplicado") {
        @Override
        public boolean requiereEvidencia() {
            return true;
        }
    },
    FALLIDO("Fallido", "fallido");

    private final String etiqueta;
    private final String claseCss;

    EstadoTecnica(String etiqueta, String claseCss) {
        this.etiqueta = etiqueta;
        this.claseCss = claseCss;
    }

    public boolean requiereEvidencia() {
        return false;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getClaseCss() {
        return claseCss;
    }
}
