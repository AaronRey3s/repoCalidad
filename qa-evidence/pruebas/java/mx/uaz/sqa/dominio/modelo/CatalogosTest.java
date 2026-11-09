package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Catálogos del dominio (RNF-02: codificación cromática)")
class CatalogosTest {

    @Test
    @DisplayName("El semáforo usa verde, amarillo y rojo con sus puntajes")
    void semaforo() {
        assertEquals("verde", EstadoSemaforo.VERDE.getClaseCss());
        assertEquals("amarillo", EstadoSemaforo.AMARILLO.getClaseCss());
        assertEquals("rojo", EstadoSemaforo.ROJO.getClaseCss());
        assertEquals(100, EstadoSemaforo.VERDE.getPuntaje());
        assertEquals(50, EstadoSemaforo.AMARILLO.getPuntaje());
        assertEquals(0, EstadoSemaforo.ROJO.getPuntaje());
        assertFalse(EstadoSemaforo.SIN_DATOS.tieneDatos());
        for (EstadoSemaforo estado : EstadoSemaforo.values()) {
            assertTrue(estado.getColorHex().startsWith("#"));
            assertFalse(estado.getEtiqueta().isBlank());
        }
    }

    @Test
    @DisplayName("Todos los catálogos tienen etiqueta legible")
    void etiquetas() {
        for (Dictamen d : Dictamen.values()) {
            assertFalse(d.getEtiqueta().isBlank());
            assertFalse(d.getClaseCss().isBlank());
            assertTrue(d.getColorHex().startsWith("#"));
        }
        for (Rol r : Rol.values()) {
            assertFalse(r.getEtiqueta().isBlank());
        }
        for (CategoriaTecnica c : CategoriaTecnica.values()) {
            assertFalse(c.getEtiqueta().isBlank());
        }
        for (Frecuencia f : Frecuencia.values()) {
            assertFalse(f.getEtiqueta().isBlank());
        }
        for (EstadoTecnica e : EstadoTecnica.values()) {
            assertFalse(e.getEtiqueta().isBlank());
            assertFalse(e.getClaseCss().isBlank());
        }
        for (SentidoMetrica s : SentidoMetrica.values()) {
            assertFalse(s.getEtiqueta().isBlank());
            assertFalse(s.reglaDeCoherencia().isBlank());
        }
    }

    @Test
    @DisplayName("Usuario: validaciones y valores por defecto")
    void usuario() {
        Usuario usuario = new Usuario("dev", "Desarrollador", "$2a$hash", Rol.DESARROLLADOR);

        assertEquals("dev", usuario.getUsername());
        assertEquals("Desarrollador", usuario.getNombreCompleto());
        assertEquals("$2a$hash", usuario.getPasswordHash());
        assertEquals(Rol.DESARROLLADOR, usuario.getRol());
        assertTrue(usuario.isActivo());
        assertThrows(DatosInvalidosException.class, () -> new Usuario("dev", "D", "h", null));
        assertThrows(DatosInvalidosException.class, () -> new Usuario(" ", "D", "h", Rol.STAKEHOLDER));
    }
}
