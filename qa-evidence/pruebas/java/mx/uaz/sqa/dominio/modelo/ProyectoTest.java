package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Proyecto por periodo (RS-01, RS-02)")
class ProyectoTest {

    @Test
    @DisplayName("Un proyecto nuevo queda activo, vinculado a su periodo y con fecha de creación")
    void creaProyectoActivo() {
        Proyecto proyecto = Fabrica.proyecto("  Plataforma SQA  ");

        assertEquals("Plataforma SQA", proyecto.getNombre());
        assertTrue(proyecto.isActivo());
        assertEquals("Ago-Dic 2026", proyecto.getPeriodo().getEtiqueta());
        assertEquals(Fabrica.AHORA, proyecto.getCreadoEn());
        assertEquals("Java, Spring Boot", proyecto.getTecnologias());
        assertEquals("Proyecto de prueba", proyecto.getDescripcion());
        assertTrue(proyecto.esNueva());
        assertNull(proyecto.getId());
    }

    @Test
    @DisplayName("Faltan nombre o periodo: se rechaza con el campo a resaltar")
    void rechazaDatosIncompletos() {
        Periodo periodo = Fabrica.periodo();
        assertEquals("nombre", assertThrows(DatosInvalidosException.class,
                () -> new Proyecto(" ", null, null, periodo, Fabrica.AHORA)).getCampo());
        assertEquals("etiquetaPeriodo", assertThrows(DatosInvalidosException.class,
                () -> new Proyecto("P", null, null, null, Fabrica.AHORA)).getCampo());
        assertEquals("nombre", assertThrows(DatosInvalidosException.class,
                () -> new Proyecto("x".repeat(121), null, null, periodo, Fabrica.AHORA)).getCampo());
    }

    @Test
    @DisplayName("RS-02: se actualizan los atributos descriptivos y el periodo")
    void actualizaDatos() {
        Proyecto proyecto = Fabrica.proyecto();
        Periodo nuevo = Periodo.de("Ene-Jun 2027", LocalDate.of(2027, 1, 11), LocalDate.of(2027, 6, 4));

        proyecto.actualizarDatos("Plataforma SQA v2", " ", "Java 21", nuevo);

        assertEquals("Plataforma SQA v2", proyecto.getNombre());
        assertNull(proyecto.getDescripcion());
        assertEquals("Java 21", proyecto.getTecnologias());
        assertEquals("Ene-Jun 2027", proyecto.getPeriodo().getEtiqueta());
    }

    @Test
    @DisplayName("Un proyecto inactivo no admite cambios y puede reactivarse")
    void activarDesactivar() {
        Proyecto proyecto = Fabrica.proyecto();

        proyecto.desactivar();
        assertFalse(proyecto.isActivo());
        assertThrows(ProyectoInactivoException.class, proyecto::verificarActivo);

        proyecto.activar();
        assertDoesNotThrow(proyecto::verificarActivo);
    }
}
