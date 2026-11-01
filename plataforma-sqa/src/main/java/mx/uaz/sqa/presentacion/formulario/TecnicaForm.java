package mx.uaz.sqa.presentacion.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.servicio.dto.DatosTecnica;

/**
 * Formulario de RS-06 (asignar técnica SQA al proyecto).
 */
public class TecnicaForm {

    @NotBlank(message = "El nombre de la técnica es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String nombre;

    @NotNull(message = "Selecciona la categoría")
    private CategoriaTecnica categoria;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @NotBlank(message = "Selecciona el responsable")
    private String responsable;

    public DatosTecnica aDatos() {
        return new DatosTecnica(nombre, categoria, descripcion, responsable);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaTecnica getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTecnica categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
}
