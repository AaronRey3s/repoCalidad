package mx.uaz.sqa.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.uaz.sqa.dominio.excepcion.PeriodoInvalidoException;
import mx.uaz.sqa.dominio.excepcion.ProyectoDuplicadoException;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.Periodo;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.servicio.dto.DatosProyecto;
import mx.uaz.sqa.servicio.dto.ProyectoResumen;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CU01 – Gestionar proyecto por periodo (servicio)")
class ProyectoServiceTest {

    private static final String PERIODO = "Ago-Dic 2026";

    @Mock
    private ProyectoRepository proyectos;

    private ProyectoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ProyectoService(proyectos, Fabrica.reloj());
    }

    private static DatosProyecto datos(String nombre, LocalDate inicio, LocalDate fin) {
        return new DatosProyecto(nombre, "Descripción", "Java", PERIODO, inicio, fin);
    }

    @Test
    @DisplayName("Dado un proyecto válido, cuando se registra, entonces se guarda vinculado al periodo")
    void registraProyecto() {
        when(proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCase("Plataforma SQA", PERIODO)).thenReturn(false);
        when(proyectos.save(any(Proyecto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Proyecto proyecto = servicio.crear(datos("Plataforma SQA", Fabrica.INICIO_PERIODO, Fabrica.FIN_PERIODO));

        assertEquals("Plataforma SQA", proyecto.getNombre());
        assertEquals(PERIODO, proyecto.getPeriodo().getEtiqueta());
        assertEquals(Fabrica.AHORA, proyecto.getCreadoEn());
        assertTrue(proyecto.isActivo());
        verify(proyectos).save(proyecto);
    }

    @Test
    @DisplayName("CU01 flujo 3b: proyecto duplicado en el periodo → se cancela el guardado")
    void rechazaDuplicado() {
        when(proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCase("Plataforma SQA", PERIODO)).thenReturn(true);

        ProyectoDuplicadoException error = assertThrows(ProyectoDuplicadoException.class,
                () -> servicio.crear(datos("Plataforma SQA", Fabrica.INICIO_PERIODO, Fabrica.FIN_PERIODO)));

        assertEquals("nombre", error.getCampo());
        verify(proyectos, never()).save(any());
    }

    @Test
    @DisplayName("CU01 flujo 3a: rango de fechas inválido → no se consulta ni guarda nada")
    void rechazaFechasInvalidas() {
        assertThrows(PeriodoInvalidoException.class,
                () -> servicio.crear(datos("Plataforma SQA", Fabrica.FIN_PERIODO, Fabrica.INICIO_PERIODO)));

        verifyNoInteractions(proyectos);
    }

    @Test
    @DisplayName("RS-02: actualiza un proyecto existente")
    void actualizaProyecto() {
        Proyecto existente = Fabrica.conId(Fabrica.proyecto(), 7L);
        when(proyectos.findById(7L)).thenReturn(Optional.of(existente));
        when(proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCaseAndIdNot("Nuevo nombre", PERIODO, 7L))
                .thenReturn(false);

        Proyecto actualizado = servicio.actualizar(7L, datos(" Nuevo nombre ", Fabrica.INICIO_PERIODO, Fabrica.FIN_PERIODO));

        assertEquals("Nuevo nombre", actualizado.getNombre());
        assertEquals("Java", actualizado.getTecnologias());
    }

    @Test
    @DisplayName("RS-02: no permite renombrar a un nombre ya usado en el mismo periodo")
    void actualizarRechazaDuplicado() {
        Proyecto existente = Fabrica.conId(Fabrica.proyecto(), 7L);
        when(proyectos.findById(7L)).thenReturn(Optional.of(existente));
        when(proyectos.existsByNombreIgnoreCaseAndPeriodoEtiquetaIgnoreCaseAndIdNot("Otro", PERIODO, 7L))
                .thenReturn(true);

        assertThrows(ProyectoDuplicadoException.class,
                () -> servicio.actualizar(7L, datos("Otro", Fabrica.INICIO_PERIODO, Fabrica.FIN_PERIODO)));
        assertEquals("Plataforma SQA", existente.getNombre());
    }

    @Test
    @DisplayName("Un proyecto inexistente produce RecursoNoEncontradoException")
    void proyectoInexistente() {
        when(proyectos.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    @DisplayName("El catálogo se agrupa por periodo respetando el orden del repositorio")
    void agrupaPorPeriodo() {
        Proyecto agosto = Fabrica.conId(Fabrica.proyecto("A"), 1L);
        Proyecto enero = Fabrica.conId(new Proyecto("B", null, null,
                Periodo.de("Ene-Jun 2027", LocalDate.of(2027, 1, 11), LocalDate.of(2027, 6, 4)), Fabrica.AHORA), 2L);
        when(proyectos.findAll(any(Sort.class))).thenReturn(List.of(enero, agosto));

        Map<String, List<ProyectoResumen>> grupos = servicio.listarAgrupadosPorPeriodo();

        assertEquals(List.of("Ene-Jun 2027", PERIODO), new ArrayList<>(grupos.keySet()));
        assertEquals(1L, grupos.get(PERIODO).get(0).id());
    }

    @Test
    @DisplayName("Activar y desactivar un proyecto")
    void cambiaActivo() {
        Proyecto existente = Fabrica.conId(Fabrica.proyecto(), 3L);
        when(proyectos.findById(3L)).thenReturn(Optional.of(existente));

        servicio.cambiarActivo(3L, false);
        assertFalse(existente.isActivo());

        servicio.cambiarActivo(3L, true);
        assertTrue(existente.isActivo());
    }

    @Test
    @DisplayName("El resumen expone los datos del proyecto para la vista")
    void obtieneResumen() {
        Proyecto existente = Fabrica.conId(Fabrica.proyecto(), 3L);
        when(proyectos.findById(3L)).thenReturn(Optional.of(existente));

        ProyectoResumen resumen = servicio.obtenerResumen(3L);

        assertEquals(3L, resumen.id());
        assertEquals(PERIODO, resumen.periodo());
        assertEquals(Fabrica.INICIO_PERIODO, resumen.fechaInicio());
        assertTrue(resumen.periodoDescripcion().startsWith(PERIODO));
        assertTrue(resumen.activo());
    }
}
