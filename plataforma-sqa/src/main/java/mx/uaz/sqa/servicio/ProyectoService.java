package mx.uaz.sqa.servicio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mx.uaz.sqa.dominio.excepcion.ProyectoDuplicadoException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.Periodo;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.Validaciones;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.servicio.dto.DatosProyecto;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU01 – Gestionar Proyecto de Software por Periodo (RS-01, RS-02).
 */
@Service
@Transactional
public class ProyectoService {

    private static final Sort ORDEN = Sort.by(Sort.Order.desc("periodo.fechaInicio"), Sort.Order.asc("nombre"));

    private final ProyectoRepository proyectos;
    private final Clock reloj;

    public ProyectoService(ProyectoRepository proyectos, Clock reloj) {
        this.proyectos = proyectos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<ProyectoResumen> listar() {
        return proyectos.findAll(ORDEN).stream().map(ProyectoResumen::desde).toList();
    }

    /** Catálogo de proyectos agrupado por etiqueta de periodo (selector del dashboard). */
    @Transactional(readOnly = true)
    public Map<String, List<ProyectoResumen>> listarAgrupadosPorPeriodo() {
        return listar().stream().collect(Collectors.groupingBy(
                ProyectoResumen::periodo, LinkedHashMap::new, Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Proyecto obtener(Long id) {
        return proyectos.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el proyecto", id));
    }

    @Transactional(readOnly = true)
    public ProyectoResumen obtenerResumen(Long id) {
        return ProyectoResumen.desde(obtener(id));
    }

    /** RS-01: registra un proyecto vinculado obligatoriamente a un periodo. */
    public Proyecto crear(DatosProyecto datos) {
        Periodo periodo = Periodo.de(datos.etiquetaPeriodo(), datos.fechaInicio(), datos.fechaFin());
        Proyecto nuevo = new Proyecto(datos.nombre(), datos.descripcion(), datos.tecnologias(), periodo,
                LocalDateTime.now(reloj));
        if (proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCase(nuevo.getNombre(), periodo.getEtiqueta())) {
            throw duplicado(nuevo.getNombre(), periodo);
        }
        return proyectos.save(nuevo);
    }

    /** RS-02: actualiza nombre, descripción, tecnologías y periodo. */
    public Proyecto actualizar(Long id, DatosProyecto datos) {
        Proyecto proyecto = obtener(id);
        Periodo periodo = Periodo.de(datos.etiquetaPeriodo(), datos.fechaInicio(), datos.fechaFin());
        String nombre = Validaciones.requerido(datos.nombre(), "El nombre del proyecto es obligatorio", "nombre", 120);
        if (proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCaseAndIdNot(nombre, periodo.getEtiqueta(), id)) {
            throw duplicado(nombre, periodo);
        }
        proyecto.actualizarDatos(nombre, datos.descripcion(), datos.tecnologias(), periodo);
        return proyecto;
    }

    public void cambiarActivo(Long id, boolean activo) {
        Proyecto proyecto = obtener(id);
        if (activo) {
            proyecto.activar();
        } else {
            proyecto.desactivar();
        }
    }

    private static ProyectoDuplicadoException duplicado(String nombre, Periodo periodo) {
        return new ProyectoDuplicadoException("El proyecto \"" + nombre + "\" ya se encuentra registrado en el periodo \""
                + periodo.getEtiqueta() + "\"; no se guardaron cambios", "nombre");
    }
}
