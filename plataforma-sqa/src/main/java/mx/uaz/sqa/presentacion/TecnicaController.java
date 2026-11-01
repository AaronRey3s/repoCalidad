package mx.uaz.sqa.presentacion;

import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import mx.uaz.sqa.dominio.excepcion.ReglaNegocioException;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.presentacion.formulario.EstadoTecnicaForm;
import mx.uaz.sqa.presentacion.formulario.TecnicaForm;
import mx.uaz.sqa.servicio.ProyectoService;
import mx.uaz.sqa.servicio.TecnicaSqaService;
import mx.uaz.sqa.servicio.UsuarioService;
import mx.uaz.sqa.servicio.dto.ArchivoAdjunto;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CU04 – Gestionar Ejecución y Evidencia de Técnica SQA.
 */
@Controller
public class TecnicaController extends ControladorBase {

    private final ProyectoService proyectoService;
    private final TecnicaSqaService tecnicaService;
    private final UsuarioService usuarioService;

    public TecnicaController(ProyectoService proyectoService, TecnicaSqaService tecnicaService,
                             UsuarioService usuarioService) {
        this.proyectoService = proyectoService;
        this.tecnicaService = tecnicaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/proyectos/{proyectoId}/tecnicas/nueva")
    @PreAuthorize("hasRole('LIDER_SQA')")
    public String nueva(@PathVariable("proyectoId") Long proyectoId, Model model) {
        model.addAttribute("form", new TecnicaForm());
        return mostrarFormularioAsignacion(proyectoId, model);
    }

    @PostMapping("/proyectos/{proyectoId}/tecnicas")
    @PreAuthorize("hasRole('LIDER_SQA')")
    public String asignar(@PathVariable("proyectoId") Long proyectoId, @Valid @ModelAttribute("form") TecnicaForm form,
                          BindingResult resultado, Principal principal, Model model, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                TecnicaSqa tecnica = tecnicaService.asignar(proyectoId, form.aDatos(), principal.getName());
                exito(atributos, "Técnica \"" + tecnica.getNombre() + "\" asignada en estado Pendiente");
                return "redirect:/proyectos/" + proyectoId;
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        return mostrarFormularioAsignacion(proyectoId, model);
    }

    @GetMapping("/tecnicas/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        EstadoTecnicaForm form = new EstadoTecnicaForm();
        form.setEstado(tecnicaService.obtener(id).getEstado());
        model.addAttribute("form", form);
        return mostrarDetalle(id, model);
    }

    @PostMapping("/tecnicas/{id}/estado")
    @PreAuthorize("hasAnyRole('LIDER_SQA', 'DESARROLLADOR')")
    public String actualizarEstado(@PathVariable("id") Long id, @Valid @ModelAttribute("form") EstadoTecnicaForm form,
                                   BindingResult resultado,
                                   @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                   Principal principal, Model model, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                TecnicaSqa tecnica = tecnicaService.actualizarEstado(id, form.getEstado(), form.getEvidenciaUrl(),
                        aAdjunto(archivo), principal.getName());
                exito(atributos, "Estado actualizado a \"" + tecnica.getEstado().getEtiqueta()
                        + "\". Cobertura de técnicas recalculada.");
                return "redirect:/tecnicas/" + id;
            } catch (ReglaNegocioException e) {
                registrarError(resultado, e);
            }
        }
        return mostrarDetalle(id, model);
    }

    @GetMapping("/tecnicas/{id}/evidencia")
    public ResponseEntity<byte[]> descargarEvidencia(@PathVariable("id") Long id) {
        ArchivoDescargable archivo = tecnicaService.descargarEvidencia(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo.contenido());
    }

    private String mostrarFormularioAsignacion(Long proyectoId, Model model) {
        model.addAttribute("proyecto", proyectoService.obtenerResumen(proyectoId));
        model.addAttribute("categorias", CategoriaTecnica.values());
        model.addAttribute("usuarios", usuarioService.listar());
        return "tecnicas/formulario";
    }

    private String mostrarDetalle(Long id, Model model) {
        TecnicaSqa tecnica = tecnicaService.obtener(id);
        model.addAttribute("tecnica", tecnica);
        model.addAttribute("proyecto", ProyectoResumen.desde(tecnica.getProyecto()));
        model.addAttribute("historial", tecnicaService.historial(id));
        model.addAttribute("estados", EstadoTecnica.values());
        return "tecnicas/detalle";
    }

    private static ArchivoAdjunto aAdjunto(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            return new ArchivoAdjunto(archivo.getOriginalFilename(), archivo.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer el archivo adjunto", e);
        }
    }
}
