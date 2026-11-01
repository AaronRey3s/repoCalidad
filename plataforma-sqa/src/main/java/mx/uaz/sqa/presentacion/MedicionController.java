package mx.uaz.sqa.presentacion;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mx.uaz.sqa.dominio.excepcion.ReglaNegocioException;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.presentacion.formulario.MedicionForm;
import mx.uaz.sqa.servicio.MedicionService;
import mx.uaz.sqa.servicio.MetricaService;
import mx.uaz.sqa.servicio.dto.IndicadorMetrica;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CU03 – Registrar Medición de Métrica.
 */
@Controller
@RequestMapping("/metricas/{id}/mediciones")
public class MedicionController extends ControladorBase {

    private final MetricaService metricaService;
    private final MedicionService medicionService;

    public MedicionController(MetricaService metricaService, MedicionService medicionService) {
        this.metricaService = metricaService;
        this.medicionService = medicionService;
    }

    @GetMapping
    public String ver(@PathVariable("id") Long id, Model model) {
        model.addAttribute("form", new MedicionForm());
        return mostrar(id, model);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'LIDER_SQA')")
    public String registrar(@PathVariable("id") Long id, @Valid @ModelAttribute("form") MedicionForm form,
                            BindingResult resultado, Principal principal, Model model,
                            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                Medicion medicion = medicionService.registrar(id, form.getValor(), form.getObservacion(),
                        principal.getName());
                exito(atributos, "Medición registrada: " + medicion.valorFormateado() + " → "
                        + medicion.getEstado().getEtiqueta());
                return "redirect:/metricas/" + id + "/mediciones";
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        return mostrar(id, model);
    }

    private String mostrar(Long id, Model model) {
        Metrica metrica = metricaService.obtener(id);
        List<Medicion> historial = medicionService.historial(id);
        List<Medicion> recientesPrimero = new ArrayList<>(historial);
        Collections.reverse(recientesPrimero);
        model.addAttribute("metrica", metrica);
        model.addAttribute("proyecto", ProyectoResumen.desde(metrica.getProyecto()));
        model.addAttribute("historial", recientesPrimero);
        model.addAttribute("grafica", GraficaTendencia.de(IndicadorMetrica.desde(metrica, historial)));
        return "metricas/mediciones";
    }
}
