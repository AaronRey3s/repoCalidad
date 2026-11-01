package mx.uaz.sqa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.uaz.sqa.dominio.excepcion.DatosInvalidosException;
import mx.uaz.sqa.dominio.excepcion.ProyectoInactivoException;
import mx.uaz.sqa.dominio.excepcion.UmbralesInvalidosException;
import mx.uaz.sqa.dominio.excepcion.ValorFueraDeDominioException;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Métrica: configuración (RS-03) y registro de mediciones (RS-04, RS-05)")
class MetricaTest {

    private final Proyecto proyecto = Fabrica.proyecto();

    @Test
    @DisplayName("Una métrica nueva inicia en estado 'Sin datos'")
    void iniciaSinDatos() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);

        assertEquals(EstadoSemaforo.SIN_DATOS, metrica.getEstadoActual());
        assertNull(metrica.getUltimoValor());
        assertEquals("—", metrica.valorActualFormateado());
        assertSame(proyecto, metrica.getProyecto());
        assertEquals(UnidadMedida.PORCENTAJE, metrica.getUnidad());
        assertEquals(Frecuencia.POR_SPRINT, metrica.getFrecuencia());
    }

    @Test
    @DisplayName("Dada una medición de 85 %, cuando se registra, entonces el semáforo pasa a Verde con autor y hora")
    void registraMedicionVerde() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);

        Medicion medicion = metrica.registrarMedicion(85, "Sprint 2", "dev", Fabrica.AHORA);

        assertEquals(EstadoSemaforo.VERDE, medicion.getEstado());
        assertEquals(85.0, medicion.getValor());
        assertEquals("dev", medicion.getAutor());
        assertEquals(Fabrica.AHORA, medicion.getRegistradoEn());
        assertEquals("Sprint 2", medicion.getObservacion());
        assertEquals("85 %", medicion.valorFormateado());
        assertSame(metrica, medicion.getMetrica());
        assertEquals(EstadoSemaforo.VERDE, metrica.getEstadoActual());
        assertEquals(85.0, metrica.getUltimoValor());
        assertEquals(Fabrica.AHORA, metrica.getUltimaMedicionEn());
        assertEquals("85 %", metrica.valorActualFormateado());
    }

    @Test
    @DisplayName("El estado actual siempre refleja la última medición registrada")
    void actualizaEstadoConCadaMedicion() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);

        metrica.registrarMedicion(85, null, "dev", Fabrica.AHORA);
        metrica.registrarMedicion(65, null, "dev", Fabrica.AHORA.plusDays(7));
        assertEquals(EstadoSemaforo.AMARILLO, metrica.getEstadoActual());

        metrica.registrarMedicion(40, null, "dev", Fabrica.AHORA.plusDays(14));
        assertEquals(EstadoSemaforo.ROJO, metrica.getEstadoActual());
    }

    @Test
    @DisplayName("Dado un porcentaje fuera de [0, 100], entonces se impide el registro y no cambia el estado")
    void rechazaValorFueraDeDominio() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);

        ValorFueraDeDominioException error = assertThrows(ValorFueraDeDominioException.class,
                () -> metrica.registrarMedicion(150, null, "dev", Fabrica.AHORA));

        assertEquals("valor", error.getCampo());
        assertEquals(EstadoSemaforo.SIN_DATOS, metrica.getEstadoActual());
    }

    @Test
    @DisplayName("No se registran mediciones en un proyecto inactivo")
    void rechazaProyectoInactivo() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);
        proyecto.desactivar();

        assertThrows(ProyectoInactivoException.class,
                () -> metrica.registrarMedicion(90, null, "dev", Fabrica.AHORA));
    }

    @Test
    @DisplayName("La medición exige autor (RNF-03)")
    void exigeAutor() {
        Metrica metrica = Fabrica.metricaCobertura(proyecto);

        assertThrows(DatosInvalidosException.class, () -> metrica.registrarMedicion(90, null, " ", Fabrica.AHORA));
    }

    @Test
    @DisplayName("Validaciones del constructor de la métrica")
    void validaConstructor() {
        assertEquals("proyecto", assertThrows(DatosInvalidosException.class, () -> new Metrica(null, "M",
                UnidadMedida.PORCENTAJE, Frecuencia.SEMANAL, SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0)).getCampo());
        assertEquals("nombre", assertThrows(DatosInvalidosException.class, () -> new Metrica(proyecto, "",
                UnidadMedida.PORCENTAJE, Frecuencia.SEMANAL, SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0)).getCampo());
        assertEquals("unidad", assertThrows(DatosInvalidosException.class, () -> new Metrica(proyecto, "M",
                null, Frecuencia.SEMANAL, SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0)).getCampo());
        assertEquals("frecuencia", assertThrows(DatosInvalidosException.class, () -> new Metrica(proyecto, "M",
                UnidadMedida.PORCENTAJE, null, SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0)).getCampo());
        assertThrows(UmbralesInvalidosException.class, () -> new Metrica(proyecto, "M",
                UnidadMedida.PORCENTAJE, Frecuencia.SEMANAL, SentidoMetrica.MAYOR_ES_MEJOR, 60.0, 80.0));
    }

    @Test
    @DisplayName("Expone los rangos de umbral formateados")
    void exponeRangos() {
        Metrica metrica = Fabrica.metricaDefectos(proyecto);

        assertEquals("≤ 1", metrica.rangoVerde());
        assertEquals("> 1 y ≤ 3", metrica.rangoAmarillo());
        assertEquals("> 3", metrica.rangoRojo());
        assertEquals(SentidoMetrica.MENOR_ES_MEJOR, metrica.getUmbrales().getSentido());
        assertEquals("Densidad de defectos", metrica.getNombre());
    }
}
