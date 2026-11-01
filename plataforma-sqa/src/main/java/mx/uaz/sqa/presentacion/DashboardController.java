package mx.uaz.sqa.presentacion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import mx.uaz.sqa.servicio.DashboardService;
import mx.uaz.sqa.servicio.ProyectoService;
import mx.uaz.sqa.servicio.dto.DashboardProyecto;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * CU05 – Visualizar Dashboard de Salud SQA. Accesible para cualquier usuario autenticado.
 */
@Controller
public class DashboardController {

    private final ProyectoService proyectoService;
    private final DashboardService dashboardService;

    public DashboardController(ProyectoService proyectoService, DashboardService dashboardService) {
        this.proyectoService = proyectoService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String ver(@RequestParam(value = "proyectoId", required = false) Long proyectoId, Model model) {
        Map<String, List<ProyectoResumen>> grupos = proyectoService.listarAgrupadosPorPeriodo();
        model.addAttribute("grupos", grupos);
        Long seleccionado = proyectoId != null ? proyectoId : primerProyecto(grupos.values());
        model.addAttribute("seleccionado", seleccionado);
        if (seleccionado != null) {
            DashboardProyecto dashboard = dashboardService.construir(seleccionado);
            model.addAttribute("d", dashboard);
            model.addAttribute("tarjetas", dashboard.metricas().stream().map(TarjetaMetrica::de).toList());
        }
        return "dashboard/ver";
    }

    private static Long primerProyecto(Collection<List<ProyectoResumen>> grupos) {
        List<ProyectoResumen> todos = grupos.stream().flatMap(List::stream).toList();
        return todos.stream().filter(ProyectoResumen::activo).findFirst()
                .or(() -> todos.stream().findFirst())
                .map(ProyectoResumen::id)
                .orElse(null);
    }
}
