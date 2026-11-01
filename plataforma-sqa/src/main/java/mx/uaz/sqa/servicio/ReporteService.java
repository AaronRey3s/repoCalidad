package mx.uaz.sqa.servicio;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import mx.uaz.sqa.dominio.excepcion.ReporteSinDatosException;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import mx.uaz.sqa.servicio.dto.FilaMedicionReporte;
import mx.uaz.sqa.servicio.dto.FilaMetricaReporte;
import mx.uaz.sqa.servicio.dto.FilaTecnicaReporte;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import mx.uaz.sqa.servicio.dto.ReporteSalud;
import mx.uaz.sqa.servicio.reporte.GeneradorReporte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU06 – Exportar Reporte del Estado de Salud del Software (RS-10, RS-11).
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final LectorSaludProyecto lector;
    private final GeneradorReporte generador;
    private final Clock reloj;

    public ReporteService(LectorSaludProyecto lector, GeneradorReporte generador, Clock reloj) {
        this.lector = lector;
        this.generador = generador;
        this.reloj = reloj;
    }

    /** RS-10: compila datos generales, métricas, técnicas con evidencias e índice global. */
    public ReporteSalud consolidar(Long proyectoId, String emitidoPor) {
        InstantaneaSalud instantanea = lector.leer(proyectoId);
        if (instantanea.sinRegistros()) {
            throw new ReporteSinDatosException("No existen registros suficientes para emitir el reporte: el proyecto \""
                    + instantanea.proyecto().getNombre() + "\" no tiene mediciones ni técnicas SQA en el periodo \""
                    + instantanea.proyecto().getPeriodo().getEtiqueta() + "\"");
        }
        List<FilaMetricaReporte> filasMetricas = instantanea.metricas().stream()
                .map(m -> filaMetrica(m, instantanea))
                .toList();
        List<FilaTecnicaReporte> filasTecnicas = instantanea.tecnicas().stream()
                .map(ReporteService::filaTecnica)
                .toList();
        return new ReporteSalud(ProyectoResumen.desde(instantanea.proyecto()), LocalDateTime.now(reloj),
                emitidoPor, filasMetricas, filasTecnicas, instantanea.salud());
    }

    /** RS-11: genera el PDF a partir de la estructura consolidada. */
    public ArchivoDescargable exportarPdf(Long proyectoId, String emitidoPor) {
        ReporteSalud reporte = consolidar(proyectoId, emitidoPor);
        byte[] pdf = generador.generar(reporte);
        return new ArchivoDescargable(nombreArchivo(reporte), pdf);
    }

    static String nombreArchivo(ReporteSalud reporte) {
        return "reporte-salud-" + slug(reporte.proyecto().nombre()) + "-" + slug(reporte.proyecto().periodo()) + ".pdf";
    }

    static String slug(String texto) {
        String sinAcentos = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String limpio = sinAcentos.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        return limpio.isEmpty() ? "sin-nombre" : limpio.toLowerCase(Locale.ROOT);
    }

    private static FilaMetricaReporte filaMetrica(Metrica metrica, InstantaneaSalud instantanea) {
        List<FilaMedicionReporte> historial = instantanea.mediciones(metrica).stream()
                .map(m -> new FilaMedicionReporte(m.getRegistradoEn(), m.valorFormateado(), m.getEstado(), m.getAutor()))
                .toList();
        return new FilaMetricaReporte(metrica.getNombre(), metrica.getUnidad().getEtiqueta(), metrica.rangoVerde(),
                metrica.rangoAmarillo(), metrica.rangoRojo(), metrica.valorActualFormateado(),
                metrica.getEstadoActual(), historial);
    }

    private static FilaTecnicaReporte filaTecnica(TecnicaSqa tecnica) {
        return new FilaTecnicaReporte(tecnica.getNombre(), tecnica.getCategoria().getEtiqueta(), tecnica.getEstado(),
                tecnica.getResponsable(), tecnica.getEvidencia().describir());
    }
}
