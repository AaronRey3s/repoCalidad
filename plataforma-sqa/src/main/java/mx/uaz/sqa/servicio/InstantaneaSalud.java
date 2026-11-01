package mx.uaz.sqa.servicio;

import java.util.List;
import java.util.Map;
import mx.uaz.sqa.dominio.modelo.IndiceSalud;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;

/**
 * Fotografía del estado SQA de un proyecto en un momento dado; la usan el dashboard (CU05)
 * y el reporte (CU06) para que ambos muestren exactamente los mismos números.
 */
public record InstantaneaSalud(Proyecto proyecto, List<Metrica> metricas,
                               Map<Metrica, List<Medicion>> medicionesPorMetrica,
                               List<TecnicaSqa> tecnicas, IndiceSalud salud) {

    public List<Medicion> mediciones(Metrica metrica) {
        return medicionesPorMetrica.getOrDefault(metrica, List.of());
    }

    /** CU06 flujo 2a: no hay mediciones ni técnicas registradas. */
    public boolean sinRegistros() {
        boolean sinMediciones = medicionesPorMetrica.values().stream().allMatch(List::isEmpty);
        return sinMediciones && tecnicas.isEmpty();
    }
}
