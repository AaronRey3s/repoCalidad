package mx.uaz.sqa.presentacion.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import mx.uaz.sqa.servicio.dto.DatosProyecto;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de CU01 (registro y edición de proyecto).
 */
public class ProyectoForm {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String descripcion;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String tecnologias;

    @NotBlank(message = "La etiqueta del periodo es obligatoria")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String etiquetaPeriodo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    public static ProyectoForm desde(ProyectoResumen proyecto) {
        ProyectoForm form = new ProyectoForm();
        form.setNombre(proyecto.nombre());
        form.setDescripcion(proyecto.descripcion());
        form.setTecnologias(proyecto.tecnologias());
        form.setEtiquetaPeriodo(proyecto.periodo());
        form.setFechaInicio(proyecto.fechaInicio());
        form.setFechaFin(proyecto.fechaFin());
        return form;
    }

    public DatosProyecto aDatos() {
        return new DatosProyecto(nombre, descripcion, tecnologias, etiquetaPeriodo, fechaInicio, fechaFin);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTecnologias() {
        return tecnologias;
    }

    public void setTecnologias(String tecnologias) {
        this.tecnologias = tecnologias;
    }

    public String getEtiquetaPeriodo() {
        return etiquetaPeriodo;
    }

    public void setEtiquetaPeriodo(String etiquetaPeriodo) {
        this.etiquetaPeriodo = etiquetaPeriodo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
