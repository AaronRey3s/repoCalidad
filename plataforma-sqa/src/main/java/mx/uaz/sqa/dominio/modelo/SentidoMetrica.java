package mx.uaz.sqa.dominio.modelo;

/**
 * Indica si un valor más alto de la métrica es mejor (p. ej. cobertura) o peor
 * (p. ej. densidad de defectos). Cada sentido implementa polimórficamente cómo se
 * evalúa el semáforo y qué orden deben tener los límites.
 *
 * <p>Con dos límites (óptimo y crítico) los tres rangos Verde / Amarillo / Rojo quedan
 * <b>mutuamente excluyentes y continuos</b> por construcción (CU02 paso 3): todo valor
 * real cae exactamente en un rango.</p>
 */
public enum SentidoMetrica {

    MAYOR_ES_MEJOR("Mayor es mejor (p. ej. cobertura)") {
        @Override
        public EstadoSemaforo evaluar(double valor, double optimo, double critico) {
            if (valor >= optimo) {
                return EstadoSemaforo.VERDE;
            }
            if (valor < critico) {
                return EstadoSemaforo.ROJO;
            }
            return EstadoSemaforo.AMARILLO;
        }

        @Override
        public boolean limitesCoherentes(double optimo, double critico) {
            return optimo > critico;
        }

        @Override
        public String reglaDeCoherencia() {
            return "el límite óptimo debe ser MAYOR que el límite crítico";
        }

        @Override
        public String rangoVerde(String optimo, String critico) {
            return "≥ " + optimo;
        }

        @Override
        public String rangoAmarillo(String optimo, String critico) {
            return "≥ " + critico + " y < " + optimo;
        }

        @Override
        public String rangoRojo(String optimo, String critico) {
            return "< " + critico;
        }
    },

    MENOR_ES_MEJOR("Menor es mejor (p. ej. defectos)") {
        @Override
        public EstadoSemaforo evaluar(double valor, double optimo, double critico) {
            if (valor <= optimo) {
                return EstadoSemaforo.VERDE;
            }
            if (valor > critico) {
                return EstadoSemaforo.ROJO;
            }
            return EstadoSemaforo.AMARILLO;
        }

        @Override
        public boolean limitesCoherentes(double optimo, double critico) {
            return optimo < critico;
        }

        @Override
        public String reglaDeCoherencia() {
            return "el límite óptimo debe ser MENOR que el límite crítico";
        }

        @Override
        public String rangoVerde(String optimo, String critico) {
            return "≤ " + optimo;
        }

        @Override
        public String rangoAmarillo(String optimo, String critico) {
            return "> " + optimo + " y ≤ " + critico;
        }

        @Override
        public String rangoRojo(String optimo, String critico) {
            return "> " + critico;
        }
    };

    private final String etiqueta;

    SentidoMetrica(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public abstract EstadoSemaforo evaluar(double valor, double optimo, double critico);

    public abstract boolean limitesCoherentes(double optimo, double critico);

    public abstract String reglaDeCoherencia();

    public abstract String rangoVerde(String optimo, String critico);

    public abstract String rangoAmarillo(String optimo, String critico);

    public abstract String rangoRojo(String optimo, String critico);

    public String getEtiqueta() {
        return etiqueta;
    }
}
