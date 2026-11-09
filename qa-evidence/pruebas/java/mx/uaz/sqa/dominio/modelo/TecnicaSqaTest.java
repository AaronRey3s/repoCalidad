package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.EvidenciaRequeridaException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Técnica SQA: estados (RS-07) y evidencia obligatoria (RS-08)")
class TecnicaSqaTest {

    private final Proyecto proyecto = Fabrica.proyecto();

    @Test
    @DisplayName("Una técnica recién asignada está Pendiente y sin evidencia")
    void iniciaPendiente() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        assertEquals(EstadoTecnica.PENDIENTE, tecnica.getEstado());
        assertFalse(tecnica.getEvidencia().tieneRespaldo());
        assertEquals("sqa", tecnica.getResponsable());
        assertEquals(CategoriaTecnica.REVISION_TECNICA_PARES, tecnica.getCategoria());
        assertSame(proyecto, tecnica.getProyecto());
        assertNull(tecnica.getActualizadoEn());
    }

    @Test
    @DisplayName("La asignación genera un registro de auditoría sin estado anterior")
    void registraAsignacion() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        CambioEstadoTecnica cambio = tecnica.registrarAsignacion("sqa", Fabrica.AHORA);

        assertNull(cambio.getEstadoAnterior());
        assertEquals(EstadoTecnica.PENDIENTE, cambio.getEstadoNuevo());
        assertEquals("sqa", cambio.getAutor());
        assertEquals(Fabrica.AHORA, cambio.getRegistradoEn());
        assertSame(tecnica, cambio.getTecnica());
        assertTrue(cambio.getDetalle().contains("responsable"));
    }

    @Test
    @DisplayName("Dado 'Aplicado' sin evidencia, cuando se cambia el estado, entonces se bloquea (CU04 flujo 3a)")
    void bloqueaAplicadoSinEvidencia() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        EvidenciaRequeridaException error = assertThrows(EvidenciaRequeridaException.class,
                () -> tecnica.cambiarEstado(EstadoTecnica.APLICADO, Evidencia.ninguna(), "dev", Fabrica.AHORA));

        assertEquals("evidenciaUrl", error.getCampo());
        assertEquals(EstadoTecnica.PENDIENTE, tecnica.getEstado());
    }

    @Test
    @DisplayName("Dado 'Aplicado' con URL válida, entonces se acepta y se registra el cambio")
    void aceptaAplicadoConUrl() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        CambioEstadoTecnica cambio = tecnica.cambiarEstado(EstadoTecnica.APLICADO,
                Evidencia.deUrl("https://github.com/riagalv/repoCalidad/pull/1"), "dev", Fabrica.AHORA);

        assertEquals(EstadoTecnica.APLICADO, tecnica.getEstado());
        assertEquals(EstadoTecnica.PENDIENTE, cambio.getEstadoAnterior());
        assertEquals(EstadoTecnica.APLICADO, cambio.getEstadoNuevo());
        assertEquals("dev", cambio.getAutor());
        assertEquals(Fabrica.AHORA, tecnica.getActualizadoEn());
        assertTrue(cambio.getDetalle().startsWith("URL: "));
    }

    @Test
    @DisplayName("Dado 'Aplicado' con archivo adjunto, entonces se acepta")
    void aceptaAplicadoConArchivo() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        tecnica.cambiarEstado(EstadoTecnica.APLICADO, Evidencia.ninguna().conArchivo("k.pdf", "acta.pdf"),
                "sqa", Fabrica.AHORA);

        assertTrue(tecnica.getEvidencia().tieneArchivo());
    }

    @Test
    @DisplayName("Si la técnica ya tenía evidencia, puede pasar a 'Aplicado' sin adjuntar otra")
    void reutilizaEvidenciaExistente() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);
        tecnica.cambiarEstado(EstadoTecnica.EN_PROGRESO, Evidencia.deUrl("https://github.com/x"), "dev", Fabrica.AHORA);

        tecnica.cambiarEstado(EstadoTecnica.APLICADO, null, "dev", Fabrica.AHORA.plusHours(1));

        assertEquals(EstadoTecnica.APLICADO, tecnica.getEstado());
    }

    @Test
    @DisplayName("Los estados Pendiente, En progreso y Fallido no exigen evidencia")
    void otrosEstadosSinEvidencia() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        tecnica.cambiarEstado(EstadoTecnica.EN_PROGRESO, Evidencia.ninguna(), "dev", Fabrica.AHORA);
        tecnica.cambiarEstado(EstadoTecnica.FALLIDO, Evidencia.ninguna(), "dev", Fabrica.AHORA);
        tecnica.cambiarEstado(EstadoTecnica.PENDIENTE, Evidencia.ninguna(), "dev", Fabrica.AHORA);

        assertEquals(EstadoTecnica.PENDIENTE, tecnica.getEstado());
    }

    @Test
    @DisplayName("El nuevo estado es obligatorio y el proyecto debe estar activo")
    void validacionesDeCambio() {
        TecnicaSqa tecnica = Fabrica.tecnica(proyecto);

        assertEquals("estado", assertThrows(DatosInvalidosException.class,
                () -> tecnica.cambiarEstado(null, Evidencia.ninguna(), "dev", Fabrica.AHORA)).getCampo());

        proyecto.desactivar();
        assertThrows(ProyectoInactivoException.class,
                () -> tecnica.cambiarEstado(EstadoTecnica.EN_PROGRESO, Evidencia.ninguna(), "dev", Fabrica.AHORA));
    }

    @Test
    @DisplayName("Validaciones del constructor")
    void validaConstructor() {
        assertThrows(DatosInvalidosException.class,
                () -> new TecnicaSqa(null, "T", CategoriaTecnica.PRUEBAS_SOFTWARE, null, "dev"));
        assertThrows(DatosInvalidosException.class,
                () -> new TecnicaSqa(proyecto, " ", CategoriaTecnica.PRUEBAS_SOFTWARE, null, "dev"));
        assertThrows(DatosInvalidosException.class,
                () -> new TecnicaSqa(proyecto, "T", null, null, "dev"));
        assertThrows(DatosInvalidosException.class,
                () -> new TecnicaSqa(proyecto, "T", CategoriaTecnica.PRUEBAS_SOFTWARE, null, ""));
        TecnicaSqa valida = new TecnicaSqa(proyecto, "T", CategoriaTecnica.PRUEBAS_SOFTWARE, "  ", "dev");
        assertNull(valida.getDescripcion());
        assertEquals("T", valida.getNombre());
    }

    @Test
    @DisplayName("Solo el estado Aplicado requiere evidencia")
    void reglaRequiereEvidencia() {
        assertTrue(EstadoTecnica.APLICADO.requiereEvidencia());
        assertFalse(EstadoTecnica.PENDIENTE.requiereEvidencia());
        assertFalse(EstadoTecnica.EN_PROGRESO.requiereEvidencia());
        assertFalse(EstadoTecnica.FALLIDO.requiereEvidencia());
    }
}
