package mx.uaz.sqa.presentacion;

import jakarta.validation.Valid;
import mx.uaz.sqa.dominio.excepcion.ReglaNegocioException;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.presentacion.formulario.ProyectoForm;
import mx.uaz.sqa.servicio.MetricaService;
import mx.uaz.sqa.servicio.ProyectoService;
import mx.uaz.sqa.servicio.TecnicaSqaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CU01 – Gestionar Proyecto de Software por Periodo.
 */
@Controller
@RequestMapping("/proyectos")
public class ProyectoController extends ControladorBase {

    private static final String VISTA_FORMULARIO = "proyectos/formulario";

    private final ProyectoService proyectoService;
    private final MetricaService metricaService;
    private final TecnicaSqaService tecnicaService;

    public ProyectoController(ProyectoService proyectoService, MetricaService metricaService,
                              TecnicaSqaService tecnicaService) {
        this.proyectoService = proyectoService;
        this.metricaService = metricaService;
        this.tecnicaService = tecnicaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("grupos", proyectoService.listarAgrupadosPorPeriodo());
        return "proyectos/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('LIDER_PROYECTO')")
    public String nuevo(Model model) {
        model.addAttribute("form", new ProyectoForm());
        model.addAttribute("proyectoId", null);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIDER_PROYECTO')")
    public String crear(@Valid @ModelAttribute("form") ProyectoForm form, BindingResult resultado,
                        Model model, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                Proyecto proyecto = proyectoService.crear(form.aDatos());
                exito(atributos, "Proyecto \"" + proyecto.getNombre() + "\" registrado en el periodo "
                        + proyecto.getPeriodo().getEtiqueta());
                return "redirect:/proyectos/" + proyecto.getId();
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        model.addAttribute("proyectoId", null);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        model.addAttribute("proyecto", proyectoService.obtenerResumen(id));
        model.addAttribute("metricas", metricaService.listarPorProyecto(id));
        model.addAttribute("tecnicas", tecnicaService.listarPorProyecto(id));
        return "proyectos/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasRole('LIDER_PROYECTO')")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("form", ProyectoForm.desde(proyectoService.obtenerResumen(id)));
        model.addAttribute("proyectoId", id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('LIDER_PROYECTO')")
    public String actualizar(@PathVariable("id") Long id, @Valid @ModelAttribute("form") ProyectoForm form,
                             BindingResult resultado, Model model, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                proyectoService.actualizar(id, form.aDatos());
                exito(atributos, "Proyecto actualizado");
                return "redirect:/proyectos/" + id;
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        model.addAttribute("proyectoId", id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/activo")
    @PreAuthorize("hasRole('LIDER_PROYECTO')")
    public String cambiarActivo(@PathVariable("id") Long id, @RequestParam("activo") boolean activo,
                                RedirectAttributes atributos) {
        proyectoService.cambiarActivo(id, activo);
        exito(atributos, activo ? "Proyecto reactivado" : "Proyecto desactivado: ya no admite cambios");
        return "redirect:/proyectos/" + id;
    }
}
