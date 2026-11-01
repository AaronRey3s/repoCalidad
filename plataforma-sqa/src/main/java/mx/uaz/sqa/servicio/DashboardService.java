package mx.uaz.sqa.servicio;

import java.util.List;
import mx.uaz.sqa.servicio.dto.DashboardProyecto;
import mx.uaz.sqa.servicio.dto.IndicadorMetrica;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import mx.uaz.sqa.servicio.dto.ResumenTecnicas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU05 – Visualizar Dashboard de Salud SQA (RS-09).
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final LectorSaludProyecto lector;

    public DashboardService(LectorSaludProyecto lector) {
        this.lector = lector;
    }

    public DashboardProyecto construir(Long proyectoId) {
        InstantaneaSalud instantanea = lector.leer(proyectoId);
        List<IndicadorMetrica> indicadores = instantanea.metricas().stream()
                .map(m -> IndicadorMetrica.desde(m, instantanea.mediciones(m)))
                .toList();
        ResumenTecnicas resumen = ResumenTecnicas.desde(instantanea.tecnicas(),
                instantanea.salud().porcentajeTecnicasAplicadas());
        return new DashboardProyecto(ProyectoResumen.desde(instantanea.proyecto()), indicadores, resumen,
                instantanea.salud());
    }
}
