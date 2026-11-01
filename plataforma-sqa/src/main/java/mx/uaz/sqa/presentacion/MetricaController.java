package mx.uaz.sqa.presentacion;

import jakarta.validation.Valid;
import mx.uaz.sqa.dominio.excepcion.ReglaNegocioException;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;
import mx.uaz.sqa.presentacion.formulario.MetricaForm;
import mx.uaz.sqa.servicio.MetricaService;
import mx.uaz.sqa.servicio.ProyectoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CU02 – Configurar Métricas de Calidad del Proyecto.
 */
@Controller
public class MetricaController extends ControladorBase {

    private final ProyectoService proyectoService;
    private final MetricaService metricaService;

    public MetricaController(ProyectoService proyectoService, MetricaService metricaService) {
        this.proyectoService = proyectoService;
        this.metricaService = metricaService;
    }

    @GetMapping("/proyectos/{proyectoId}/metricas/nueva")
    @PreAuthorize("hasRole('LIDER_SQA')")
    public String nueva(@PathVariable("proyectoId") Long proyectoId, Model model) {
        model.addAttribute("form", new MetricaForm());
        return mostrarFormulario(proyectoId, model);
    }

    @PostMapping("/proyectos/{proyectoId}/metricas")
    @PreAuthorize("hasRole('LIDER_SQA')")
    public String configurar(@PathVariable("proyectoId") Long proyectoId,
                             @Valid @ModelAttribute("form") MetricaForm form, BindingResult resultado,
                             Model model, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                Metrica metrica = metricaService.configurar(proyectoId, form.aDatos());
                exito(atributos, "Métrica \"" + metrica.getNombre() + "\" configurada. Verde " + metrica.rangoVerde()
                        + " · Amarillo " + metrica.rangoAmarillo() + " · Rojo " + metrica.rangoRojo());
                return "redirect:/proyectos/" + proyectoId;
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        return mostrarFormulario(proyectoId, model);
    }

    @PostMapping("/metricas/{id}/eliminar")
    @PreAuthorize("hasRole('LIDER_SQA')")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes atributos) {
        Long proyectoId = metricaService.obtener(id).getProyecto().getId();
        try {
            metricaService.eliminar(id);
            exito(atributos, "Métrica eliminada");
        } catch (ReglaNegocioException e) {
            error(atributos, e.getMessage());
        }
        return "redirect:/proyectos/" + proyectoId;
    }

    private String mostrarFormulario(Long proyectoId, Model model) {
        model.addAttribute("proyecto", proyectoService.obtenerResumen(proyectoId));
        model.addAttribute("unidades", UnidadMedida.values());
        model.addAttribute("frecuencias", Frecuencia.values());
        model.addAttribute("sentidos", SentidoMetrica.values());
        return "metricas/formulario";
    }
}
