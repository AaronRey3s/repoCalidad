package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;

/**
 * Proyecto de software evaluado en un periodo (RS-01, RS-02).
 * Un mismo nombre no puede repetirse dentro del mismo periodo (CU01 flujo 3b).
 */
@Entity
@Table(name = "proyecto", uniqueConstraints = @UniqueConstraint(
        name = "uk_proyecto_nombre_periodo", columnNames = {"nombre", "periodo_etiqueta"}))
public class Proyecto extends EntidadPersistente {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 300)
    private String tecnologias;

    @Embedded
    private Periodo periodo;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected Proyecto() {
        // Requerido por JPA
    }

    public Proyecto(String nombre, String descripcion, String tecnologias, Periodo periodo,
                    LocalDateTime creadoEn) {
        asignarDatos(nombre, descripcion, tecnologias, periodo);
        this.creadoEn = Objects.requireNonNull(creadoEn, "La fecha de creación es obligatoria");
        this.activo = true;
    }

    /** RS-02: actualiza los atributos descriptivos del proyecto. */
    public void actualizarDatos(String nombre, String descripcion, String tecnologias, Periodo periodo) {
        asignarDatos(nombre, descripcion, tecnologias, periodo);
    }

    private void asignarDatos(String nombre, String descripcion, String tecnologias, Periodo periodo) {
        String nombreValidado = Validaciones.requerido(nombre, "El nombre del proyecto es obligatorio", "nombre", 120);
        String descripcionValidada = Validaciones.opcional(descripcion, "descripcion", 1000);
        String tecnologiasValidadas = Validaciones.opcional(tecnologias, "tecnologias", 300);
        if (periodo == null) {
            throw new DatosInvalidosException("El proyecto debe vincularse a un periodo de evaluación", "etiquetaPeriodo");
        }
        this.nombre = nombreValidado;
        this.descripcion = descripcionValidada;
        this.tecnologias = tecnologiasValidadas;
        this.periodo = periodo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    /** Precondición de CU02, CU03 y CU04: solo un proyecto activo admite cambios. */
    public void verificarActivo() {
        if (!activo) {
            throw new ProyectoInactivoException("El proyecto \"" + nombre
                    + "\" está inactivo; reactívalo para registrar cambios.");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTecnologias() {
        return tecnologias;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
