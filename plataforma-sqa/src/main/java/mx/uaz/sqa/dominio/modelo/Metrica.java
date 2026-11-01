package mx.uaz.sqa.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;

/**
 * Métrica de calidad configurada para un proyecto (RS-03) con su estado actual de
 * semáforo (RS-05).
 */
@Entity
@Table(name = "metrica", uniqueConstraints = @UniqueConstraint(
        name = "uk_metrica_proyecto_nombre", columnNames = {"proyecto_id", "nombre"}))
public class Metrica extends EntidadPersistente {

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadMedida unidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frecuencia frecuencia;

    @Embedded
    private Umbrales umbrales;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false, length = 20)
    private EstadoSemaforo estadoActual;

    @Column(name = "ultimo_valor")
    private Double ultimoValor;

    @Column(name = "ultima_medicion_en")
    private LocalDateTime ultimaMedicionEn;

    protected Metrica() {
        // Requerido por JPA
    }

    public Metrica(Proyecto proyecto, String nombre, UnidadMedida unidad, Frecuencia frecuencia,
                   SentidoMetrica sentido, Double limiteOptimo, Double limiteCritico) {
        if (proyecto == null) {
            throw new DatosInvalidosException("La métrica debe pertenecer a un proyecto", "proyecto");
        }
        String nombreValidado = Validaciones.requerido(nombre, "El nombre de la métrica es obligatorio", "nombre", 120);
        if (unidad == null) {
            throw new DatosInvalidosException("Selecciona la unidad de medida", "unidad");
        }
        if (frecuencia == null) {
            throw new DatosInvalidosException("Selecciona la frecuencia de medición", "frecuencia");
        }
        this.umbrales = Umbrales.definir(sentido, limiteOptimo, limiteCritico, unidad);
        this.proyecto = proyecto;
        this.nombre = nombreValidado;
        this.unidad = unidad;
        this.frecuencia = frecuencia;
        this.estadoActual = EstadoSemaforo.SIN_DATOS;
    }

    /**
     * CU03 / RS-04 / RS-05: registra una medición, evalúa el semáforo y actualiza el estado
     * actual de la métrica. Devuelve la medición (inmutable) que debe persistirse.
     */
    public Medicion registrarMedicion(double valor, String observacion, String autor, LocalDateTime instante) {
        proyecto.verificarActivo();
        unidad.validar(valor, "valor");
        EstadoSemaforo estado = umbrales.evaluar(valor);
        Medicion medicion = new Medicion(this, valor, estado, observacion, autor, instante);
        this.estadoActual = estado;
        this.ultimoValor = valor;
        this.ultimaMedicionEn = instante;
        return medicion;
    }

    public String valorActualFormateado() {
        return ultimoValor == null ? "—" : unidad.formatear(ultimoValor);
    }

    public String rangoVerde() {
        return umbrales.rangoVerde(unidad);
    }

    public String rangoAmarillo() {
        return umbrales.rangoAmarillo(unidad);
    }

    public String rangoRojo() {
        return umbrales.rangoRojo(unidad);
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public String getNombre() {
        return nombre;
    }

    public UnidadMedida getUnidad() {
        return unidad;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public Umbrales getUmbrales() {
        return umbrales;
    }

    public EstadoSemaforo getEstadoActual() {
        return estadoActual;
    }

    public Double getUltimoValor() {
        return ultimoValor;
    }

    public LocalDateTime getUltimaMedicionEn() {
        return ultimaMedicionEn;
    }
}
