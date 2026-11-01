package mx.uaz.sqa.servicio.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import mx.uaz.sqa.servicio.dto.FilaMedicionReporte;
import mx.uaz.sqa.servicio.dto.FilaMetricaReporte;
import mx.uaz.sqa.servicio.dto.FilaTecnicaReporte;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import mx.uaz.sqa.servicio.dto.ReporteSalud;
import org.springframework.stereotype.Component;

/**
 * RS-11: genera el reporte de salud en formato PDF con OpenPDF.
 */
@Component
public class GeneradorReportePdf implements GeneradorReporte {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color AZUL = new Color(0, 51, 102);
    private static final Color GRIS_CLARO = new Color(240, 242, 245);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 12, Font.BOLD, AZUL);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font ENCABEZADO_TABLA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);

    @Override
    public byte[] generar(ReporteSalud reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            agregarEncabezado(documento, reporte);
            agregarDictamen(documento, reporte);
            agregarMetricas(documento, reporte);
            agregarTecnicas(documento, reporte);
            agregarHistorico(documento, reporte);
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No fue posible generar el reporte PDF", e);
        }
        return salida.toByteArray();
    }

    private void agregarEncabezado(Document documento, ReporteSalud reporte) throws DocumentException {
        ProyectoResumen proyecto = reporte.proyecto();
        Paragraph titulo = new Paragraph("Reporte de Estado de Salud del Software", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        Paragraph subtitulo = new Paragraph("Plataforma de Monitoreo y Gestión SQA", PEQUENA);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(12);
        documento.add(subtitulo);

        PdfPTable datos = new PdfPTable(new float[] {1.3f, 3.7f});
        datos.setWidthPercentage(100);
        fila(datos, "Proyecto", proyecto.nombre());
        fila(datos, "Descripción", valor(proyecto.descripcion()));
        fila(datos, "Tecnologías", valor(proyecto.tecnologias()));
        fila(datos, "Periodo", proyecto.periodo() + " (" + proyecto.fechaInicio().format(FECHA)
                + " – " + proyecto.fechaFin().format(FECHA) + ")");
        fila(datos, "Emitido", reporte.emitidoEn().format(FECHA_HORA) + " por " + reporte.emitidoPor());
        documento.add(datos);
    }

    private void agregarDictamen(Document documento, ReporteSalud reporte) throws DocumentException {
        documento.add(seccion("1. Dictamen global de salud"));
        PdfPTable tabla = new PdfPTable(new float[] {1, 1, 1, 1});
        tabla.setWidthPercentage(100);
        tabla.addCell(encabezado("Índice de métricas"));
        tabla.addCell(encabezado("Técnicas aplicadas"));
        tabla.addCell(encabezado("Índice global"));
        tabla.addCell(encabezado("Dictamen"));
        tabla.addCell(celda(reporte.salud().indiceMetricasTexto()));
        tabla.addCell(celda(reporte.salud().porcentajeTecnicasTexto()));
        tabla.addCell(celda(reporte.salud().indiceGlobalTexto()));
        tabla.addCell(celdaColor(reporte.salud().dictamen().getEtiqueta(), reporte.salud().dictamen().getColorHex()));
        documento.add(tabla);
        Paragraph nota = new Paragraph("Índice global = promedio del índice de métricas (Verde 100, Amarillo 50, Rojo 0) "
                + "y del porcentaje de técnicas en estado Aplicado. Dictamen: >= 80 Saludable, >= 60 En riesgo, < 60 Crítico.",
                PEQUENA);
        nota.setSpacingBefore(4);
        documento.add(nota);
    }

    private void agregarMetricas(Document documento, ReporteSalud reporte) throws DocumentException {
        documento.add(seccion("2. Resultados de métricas"));
        if (reporte.metricas().isEmpty()) {
            documento.add(new Paragraph("El proyecto no tiene métricas configuradas.", NORMAL));
            return;
        }
        PdfPTable tabla = new PdfPTable(new float[] {3, 1.4f, 1.3f, 1.6f, 1.3f, 1.3f});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String titulo : new String[] {"Métrica", "Unidad", "Verde", "Amarillo", "Rojo", "Valor / Estado"}) {
            tabla.addCell(encabezado(titulo));
        }
        for (FilaMetricaReporte fila : reporte.metricas()) {
            tabla.addCell(celda(fila.nombre()));
            tabla.addCell(celda(fila.unidad()));
            tabla.addCell(celda(fila.rangoVerde()));
            tabla.addCell(celda(fila.rangoAmarillo()));
            tabla.addCell(celda(fila.rangoRojo()));
            tabla.addCell(celdaColor(fila.valorActual() + "\n" + fila.estado().getEtiqueta(), fila.estado().getColorHex()));
        }
        documento.add(tabla);
    }

    private void agregarTecnicas(Document documento, ReporteSalud reporte) throws DocumentException {
        documento.add(seccion("3. Técnicas SQA y evidencias"));
        if (reporte.tecnicas().isEmpty()) {
            documento.add(new Paragraph("El proyecto no tiene técnicas SQA asignadas.", NORMAL));
            return;
        }
        PdfPTable tabla = new PdfPTable(new float[] {2.6f, 1.6f, 1.1f, 1.1f, 3});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String titulo : new String[] {"Técnica", "Categoría", "Estado", "Responsable", "Evidencia"}) {
            tabla.addCell(encabezado(titulo));
        }
        for (FilaTecnicaReporte fila : reporte.tecnicas()) {
            tabla.addCell(celda(fila.nombre()));
            tabla.addCell(celda(fila.categoria()));
            tabla.addCell(celda(fila.estado().getEtiqueta()));
            tabla.addCell(celda(fila.responsable()));
            tabla.addCell(celda(fila.evidencia()));
        }
        documento.add(tabla);
    }

    private void agregarHistorico(Document documento, ReporteSalud reporte) throws DocumentException {
        documento.add(seccion("4. Histórico de mediciones"));
        boolean hayHistorico = false;
        for (FilaMetricaReporte metrica : reporte.metricas()) {
            if (metrica.historial().isEmpty()) {
                continue;
            }
            hayHistorico = true;
            Paragraph nombre = new Paragraph(metrica.nombre(), NEGRITA);
            nombre.setSpacingBefore(6);
            nombre.setSpacingAfter(3);
            documento.add(nombre);
            PdfPTable tabla = new PdfPTable(new float[] {1.6f, 1.2f, 1.2f, 1.4f});
            tabla.setWidthPercentage(80);
            tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.setHeaderRows(1);
            for (String titulo : new String[] {"Fecha", "Valor", "Estado", "Autor"}) {
                tabla.addCell(encabezado(titulo));
            }
            for (FilaMedicionReporte medicion : metrica.historial()) {
                tabla.addCell(celda(medicion.fecha().format(FECHA_HORA)));
                tabla.addCell(celda(medicion.valor()));
                tabla.addCell(celdaColor(medicion.estado().getEtiqueta(), medicion.estado().getColorHex()));
                tabla.addCell(celda(medicion.autor()));
            }
            documento.add(tabla);
        }
        if (!hayHistorico) {
            documento.add(new Paragraph("Aún no hay mediciones registradas en el periodo.", NORMAL));
        }
    }

    private static Paragraph seccion(String texto) {
        Paragraph parrafo = new Paragraph(texto, SUBTITULO);
        parrafo.setSpacingBefore(14);
        parrafo.setSpacingAfter(6);
        return parrafo;
    }

    private static void fila(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        celdaEtiqueta.setBackgroundColor(GRIS_CLARO);
        celdaEtiqueta.setPadding(4);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celda(valor));
    }

    private static PdfPCell encabezado(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, ENCABEZADO_TABLA));
        celda.setBackgroundColor(AZUL);
        celda.setPadding(4);
        return celda;
    }

    private static PdfPCell celda(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(compatibleConPdf(texto), NORMAL));
        celda.setPadding(4);
        return celda;
    }

    private static PdfPCell celdaColor(String texto, String colorHex) {
        PdfPCell celda = new PdfPCell(new Phrase(compatibleConPdf(texto), ENCABEZADO_TABLA));
        celda.setBackgroundColor(Color.decode(colorHex));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(4);
        return celda;
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    /** Las fuentes estándar del PDF (WinAnsi) no incluyen ≥ ni ≤. */
    static String compatibleConPdf(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("≥", ">=").replace("≤", "<=");
    }
}
