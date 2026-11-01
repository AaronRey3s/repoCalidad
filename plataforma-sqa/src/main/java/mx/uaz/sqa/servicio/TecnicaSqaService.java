package mx.uaz.sqa.servicio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.CambioEstadoTecnica;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.Evidencia;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.persistencia.AlmacenArchivos;
import mx.uaz.sqa.persistencia.CambioEstadoTecnicaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import mx.uaz.sqa.servicio.dto.ArchivoAdjunto;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import mx.uaz.sqa.servicio.dto.DatosTecnica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU04 – Gestionar Ejecución y Evidencia de Técnica SQA (RS-06, RS-07, RS-08).
 */
@Service
@Transactional
public class TecnicaSqaService {

    private final ProyectoRepository proyectos;
    private final TecnicaSqaRepository tecnicas;
    private final CambioEstadoTecnicaRepository cambios;
    private final AlmacenArchivos almacen;
    private final Clock reloj;

    public TecnicaSqaService(ProyectoRepository proyectos, TecnicaSqaRepository tecnicas,
                             CambioEstadoTecnicaRepository cambios, AlmacenArchivos almacen, Clock reloj) {
        this.proyectos = proyectos;
        this.tecnicas = tecnicas;
        this.cambios = cambios;
        this.almacen = almacen;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<TecnicaSqa> listarPorProyecto(Long proyectoId) {
        return tecnicas.findByProyectoIdOrderByIdAsc(proyectoId);
    }

    @Transactional(readOnly = true)
    public TecnicaSqa obtener(Long tecnicaId) {
        return tecnicas.findById(tecnicaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la técnica SQA", tecnicaId));
    }

    @Transactional(readOnly = true)
    public List<CambioEstadoTecnica> historial(Long tecnicaId) {
        return cambios.findByTecnicaIdOrderByRegistradoEnAscIdAsc(tecnicaId);
    }

    /** RS-06: asigna una técnica al proyecto en estado Pendiente y deja registro de auditoría. */
    public TecnicaSqa asignar(Long proyectoId, DatosTecnica datos, String autor) {
        Proyecto proyecto = proyectos.findById(proyectoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el proyecto", proyectoId));
        proyecto.verificarActivo();
        TecnicaSqa tecnica = tecnicas.save(new TecnicaSqa(proyecto, datos.nombre(), datos.categoria(),
                datos.descripcion(), datos.responsable()));
        cambios.save(tecnica.registrarAsignacion(autor, LocalDateTime.now(reloj)));
        return tecnica;
    }

    /**
     * RS-07 / RS-08: cambia el estado de la técnica adjuntando opcionalmente una URL y/o un archivo.
     * Si la regla de evidencia falla, el archivo recién guardado se elimina (no quedan huérfanos).
     */
    public TecnicaSqa actualizarEstado(Long tecnicaId, EstadoTecnica nuevoEstado, String urlEvidencia,
                                       ArchivoAdjunto archivo, String autor) {
        TecnicaSqa tecnica = obtener(tecnicaId);
        Evidencia evidenciaNueva = Evidencia.deUrl(urlEvidencia);
        String claveGuardada = null;
        if (archivo != null && !archivo.estaVacio()) {
            claveGuardada = almacen.guardar(archivo.nombreOriginal(), archivo.contenido());
            evidenciaNueva = evidenciaNueva.conArchivo(claveGuardada, archivo.nombreOriginal());
        }
        try {
            CambioEstadoTecnica cambio = tecnica.cambiarEstado(nuevoEstado, evidenciaNueva, autor,
                    LocalDateTime.now(reloj));
            cambios.save(cambio);
            return tecnica;
        } catch (RuntimeException e) {
            if (claveGuardada != null) {
                almacen.eliminar(claveGuardada);
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ArchivoDescargable descargarEvidencia(Long tecnicaId) {
        TecnicaSqa tecnica = obtener(tecnicaId);
        Evidencia evidencia = tecnica.getEvidencia();
        if (!evidencia.tieneArchivo()) {
            throw new RecursoNoEncontradoException("La técnica no tiene un archivo de evidencia adjunto");
        }
        return new ArchivoDescargable(evidencia.getArchivoNombre(), almacen.leer(evidencia.getArchivoClave()));
    }
}
