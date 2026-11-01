package mx.uaz.sqa.presentacion;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import mx.uaz.sqa.dominio.excepcion.ReporteSinDatosException;
import mx.uaz.sqa.servicio.ReporteService;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * CU06 – Exportar Reporte del Estado de Salud del Software.
 */
@Controller
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/proyectos/{id}/reporte.pdf")
    @PreAuthorize("hasAnyRole('LIDER_SQA', 'LIDER_PROYECTO', 'STAKEHOLDER')")
    public ResponseEntity<byte[]> exportar(@PathVariable("id") Long id, Principal principal) {
        try {
            ArchivoDescargable pdf = reporteService.exportarPdf(id, principal.getName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(pdf.nombre(), StandardCharsets.UTF_8).build().toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf.contenido());
        } catch (ReporteSinDatosException e) {
            // CU06 flujo 2a: se notifica en el detalle del proyecto y se cancela la operación
            URI destino = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/proyectos/{id}")
                    .queryParam("reporteSinDatos", "1")
                    .buildAndExpand(id)
                    .toUri();
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(destino).build();
        }
    }
}
