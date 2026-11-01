package mx.uaz.sqa.servicio.dto;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.IndiceSalud;

/**
 * Panel de salud SQA de un proyecto (CU05, RS-09).
 */
public record DashboardProyecto(ProyectoResumen proyecto, List<IndicadorMetrica> metricas,
                                ResumenTecnicas tecnicas, IndiceSalud salud) {

    /** CU05 flujo 2a: el proyecto aún no tiene mediciones registradas. */
    public boolean sinMediciones() {
        return metricas.stream().noneMatch(IndicadorMetrica::tieneDatos);
    }
}
