package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.EvidenciaRequeridaException;

/**
 * Técnica o método de aseguramiento de calidad asignado a un proyecto (RS-06),
 * con su estado de ejecución (RS-07) y su evidencia (RS-08).
 */
@Entity
@Table(name = "tecnica_sqa")
public class TecnicaSqa extends EntidadPersistente {

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaTecnica categoria;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 60)
    private String responsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTecnica estado;

    @Embedded
    private Evidencia evidencia;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    protected TecnicaSqa() {
        // Requerido por JPA
    }

    public TecnicaSqa(Proyecto proyecto, String nombre, CategoriaTecnica categoria,
                      String descripcion, String responsable) {
        if (proyecto == null) {
            throw new DatosInvalidosException("La técnica debe asignarse a un proyecto", "proyecto");
        }
        String nombreValidado = Validaciones.requerido(nombre, "El nombre de la técnica es obligatorio", "nombre", 150);
        if (categoria == null) {
            throw new DatosInvalidosException("Selecciona la categoría de la técnica", "categoria");
        }
        this.proyecto = proyecto;
        this.nombre = nombreValidado;
        this.categoria = categoria;
        this.descripcion = Validaciones.opcional(descripcion, "descripcion", 500);
        this.responsable = Validaciones.requerido(responsable, "Indica el responsable de la técnica", "responsable", 60);
        this.estado = EstadoTecnica.PENDIENTE;
        this.evidencia = Evidencia.ninguna();
    }

    /** Registro de auditoría de la asignación inicial (estado Pendiente). */
    public CambioEstadoTecnica registrarAsignacion(String autor, LocalDateTime instante) {
        return new CambioEstadoTecnica(this, null, estado, autor, instante,
                "Asignación de la técnica (responsable: " + responsable + ")");
    }

    /**
     * RS-07 / RS-08: cambia el estado de ejecución. Si el nuevo estado es Aplicado,
     * exige que exista evidencia (la nueva o la que ya tenía la técnica).
     *
     * @return el registro inmutable del cambio para auditoría (RNF-03)
     */
    public CambioEstadoTecnica cambiarEstado(EstadoTecnica nuevoEstado, Evidencia evidenciaNueva,
                                             String autor, LocalDateTime instante) {
        proyecto.verificarActivo();
        if (nuevoEstado == null) {
            throw new DatosInvalidosException("Selecciona el nuevo estado de la técnica", "estado");
        }
        Evidencia resultante = getEvidencia().combinarCon(evidenciaNueva);
        if (nuevoEstado.requiereEvidencia() && !resultante.tieneRespaldo()) {
            throw new EvidenciaRequeridaException("Para marcar la técnica como \"Aplicado\" debes adjuntar "
                    + "un archivo o una URL válida de evidencia (RS-08)", "evidenciaUrl");
        }
        EstadoTecnica anterior = this.estado;
        this.estado = nuevoEstado;
        this.evidencia = resultante;
        this.actualizadoEn = instante;
        return new CambioEstadoTecnica(this, anterior, nuevoEstado, autor, instante, resultante.describir());
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public String getNombre() {
        return nombre;
    }

    public CategoriaTecnica getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getResponsable() {
        return responsable;
    }

    public EstadoTecnica getEstado() {
        return estado;
    }

    /** Hibernate deja el objeto embebido en null cuando todas sus columnas son null. */
    public Evidencia getEvidencia() {
        return evidencia == null ? Evidencia.ninguna() : evidencia;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
