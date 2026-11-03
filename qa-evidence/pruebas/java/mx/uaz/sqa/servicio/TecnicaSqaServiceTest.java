package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.EvidenciaInvalidaException;
import mx.uaz.sqa.dominio.excepcion.EvidenciaRequeridaException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.CambioEstadoTecnica;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.persistencia.AlmacenArchivos;
import mx.uaz.sqa.persistencia.CambioEstadoTecnicaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import mx.uaz.sqa.servicio.dto.ArchivoAdjunto;
import mx.uaz.sqa.servicio.dto.ArchivoDescargable;
import mx.uaz.sqa.servicio.dto.DatosTecnica;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU04 – Gestionar ejecución y evidencia de técnica SQA (servicio)")
class TecnicaSqaServiceTest {

    private static final byte[] CONTENIDO = "acta de revisión".getBytes(StandardCharsets.UTF_8);

    @Mock
    private ProyectoRepository proyectos;
    @Mock
    private TecnicaSqaRepository tecnicas;
    @Mock
    private CambioEstadoTecnicaRepository cambios;
    @Mock
    private AlmacenArchivos almacen;

    private TecnicaSqaService servicio;
    private Proyecto proyecto;
    private TecnicaSqa tecnica;

    @BeforeEach
    void preparar() {
        servicio = new TecnicaSqaService(proyectos, tecnicas, cambios, almacen, Fabrica.reloj());
        proyecto = Fabrica.conId(Fabrica.proyecto(), 1L);
        tecnica = Fabrica.conId(Fabrica.tecnica(proyecto), 5L);
    }

    @Test
    @DisplayName("RS-06: asignar una técnica la deja Pendiente y registra la bitácora")
    void asignaTecnica() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(tecnicas.save(any(TecnicaSqa.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TecnicaSqa asignada = servicio.asignar(1L,
                new DatosTecnica("Pruebas unitarias", CategoriaTecnica.PRUEBAS_SOFTWARE, null, "dev"), "sqa");

        assertEquals(EstadoTecnica.PENDIENTE, asignada.getEstado());
        ArgumentCaptor<CambioEstadoTecnica> captor = ArgumentCaptor.forClass(CambioEstadoTecnica.class);
        verify(cambios).save(captor.capture());
        assertNull(captor.getValue().getEstadoAnterior());
        assertEquals("sqa", captor.getValue().getAutor());
        assertEquals(Fabrica.AHORA, captor.getValue().getRegistradoEn());
    }

    @Test
    @DisplayName("No se asignan técnicas a un proyecto inactivo")
    void asignarProyectoInactivo() {
        proyecto.desactivar();
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));

        assertThrows(ProyectoInactivoException.class, () -> servicio.asignar(1L,
                new DatosTecnica("T", CategoriaTecnica.AUDITORIA_PROCESO, null, "lider"), "sqa"));
        verifyNoInteractions(tecnicas, cambios);
    }

    @Test
    @DisplayName("CU04 flujo 3a: 'Aplicado' sin evidencia se bloquea y no se registra nada")
    void bloqueaAplicadoSinEvidencia() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));

        assertThrows(EvidenciaRequeridaException.class,
                () -> servicio.actualizarEstado(5L, EstadoTecnica.APLICADO, "  ", null, "dev"));

        assertEquals(EstadoTecnica.PENDIENTE, tecnica.getEstado());
        verify(cambios, never()).save(any());
        verifyNoInteractions(almacen);
    }

    @Test
    @DisplayName("'Aplicado' con URL válida: cambia el estado y registra autor y hora (RNF-03)")
    void aplicaConUrl() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));

        servicio.actualizarEstado(5L, EstadoTecnica.APLICADO, "https://github.com/riagalv/repoCalidad/pull/1", null, "dev");

        assertEquals(EstadoTecnica.APLICADO, tecnica.getEstado());
        ArgumentCaptor<CambioEstadoTecnica> captor = ArgumentCaptor.forClass(CambioEstadoTecnica.class);
        verify(cambios).save(captor.capture());
        assertEquals(EstadoTecnica.PENDIENTE, captor.getValue().getEstadoAnterior());
        assertEquals("dev", captor.getValue().getAutor());
        verifyNoInteractions(almacen);
    }

    @Test
    @DisplayName("'Aplicado' con archivo: se almacena el archivo y se vincula a la técnica")
    void aplicaConArchivo() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));
        when(almacen.guardar("acta.pdf", CONTENIDO)).thenReturn("clave-uuid.pdf");

        servicio.actualizarEstado(5L, EstadoTecnica.APLICADO, null, new ArchivoAdjunto("acta.pdf", CONTENIDO), "sqa");

        assertEquals("clave-uuid.pdf", tecnica.getEvidencia().getArchivoClave());
        assertEquals("acta.pdf", tecnica.getEvidencia().getArchivoNombre());
        verify(cambios).save(any(CambioEstadoTecnica.class));
    }

    @Test
    @DisplayName("Una URL inválida se rechaza antes de guardar cualquier archivo")
    void urlInvalidaNoGuardaArchivo() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));

        assertThrows(EvidenciaInvalidaException.class, () -> servicio.actualizarEstado(5L, EstadoTecnica.EN_PROGRESO,
                "no-es-url", new ArchivoAdjunto("acta.pdf", CONTENIDO), "dev"));
        verifyNoInteractions(almacen, cambios);
    }

    @Test
    @DisplayName("Si falla la regla de negocio, el archivo recién guardado se elimina (sin huérfanos)")
    void compensaArchivoSiFalla() {
        proyecto.desactivar();
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));
        when(almacen.guardar("acta.pdf", CONTENIDO)).thenReturn("clave-uuid.pdf");

        assertThrows(ProyectoInactivoException.class, () -> servicio.actualizarEstado(5L, EstadoTecnica.APLICADO,
                null, new ArchivoAdjunto("acta.pdf", CONTENIDO), "dev"));
        verify(almacen).eliminar("clave-uuid.pdf");
        verify(cambios, never()).save(any());
    }

    @Test
    @DisplayName("Un archivo vacío se ignora")
    void ignoraArchivoVacio() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));

        servicio.actualizarEstado(5L, EstadoTecnica.EN_PROGRESO, null, new ArchivoAdjunto("x.pdf", new byte[0]), "dev");

        assertEquals(EstadoTecnica.EN_PROGRESO, tecnica.getEstado());
        verifyNoInteractions(almacen);
    }

    @Test
    @DisplayName("Descarga del archivo de evidencia")
    void descargaEvidencia() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));
        when(almacen.guardar("acta.pdf", CONTENIDO)).thenReturn("clave-uuid.pdf");
        when(almacen.leer("clave-uuid.pdf")).thenReturn(CONTENIDO);
        servicio.actualizarEstado(5L, EstadoTecnica.APLICADO, null, new ArchivoAdjunto("acta.pdf", CONTENIDO), "sqa");

        ArchivoDescargable archivo = servicio.descargarEvidencia(5L);

        assertEquals("acta.pdf", archivo.nombre());
        assertArrayEquals(CONTENIDO, archivo.contenido());
    }

    @Test
    @DisplayName("Sin archivo adjunto no hay nada que descargar")
    void descargaSinArchivo() {
        when(tecnicas.findById(5L)).thenReturn(Optional.of(tecnica));

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.descargarEvidencia(5L));
    }

    @Test
    @DisplayName("Consultas: listado, historial y técnica inexistente")
    void consultas() {
        when(tecnicas.findByProyectoIdOrderByIdAsc(1L)).thenReturn(List.of(tecnica));
        when(cambios.findByTecnicaIdOrderByRegistradoEnAscIdAsc(5L)).thenReturn(List.of());
        when(tecnicas.findById(6L)).thenReturn(Optional.empty());

        assertEquals(1, servicio.listarPorProyecto(1L).size());
        assertEquals(0, servicio.historial(5L).size());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(6L));
    }
}
