package mx.uaz.sqa.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.servicio.dto.IndicadorMetrica;
import mx.uaz.sqa.servicio.dto.PuntoSerie;
import mx.uaz.sqa.soporte.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RS-09 – Gráfica de tendencia histórica (SVG)")
class GraficaTendenciaTest {

    private static IndicadorMetrica indicador(boolean porcentaje, double optimo, double critico, List<PuntoSerie> serie) {
        return new IndicadorMetrica(1L, "M", "u", "f", EstadoSemaforo.VERDE, "v", "a", "b", "c",
                optimo, critico, porcentaje, serie);
    }

    private static PuntoSerie punto(double valor, EstadoSemaforo estado) {
        return new PuntoSerie(Fabrica.AHORA, valor, String.valueOf(valor), estado);
    }

    @Test
    @DisplayName("Sin mediciones la gráfica queda vacía")
    void vacia() {
        GraficaTendencia grafica = GraficaTendencia.de(indicador(true, 80, 60, List.of()));

        assertTrue(grafica.vacia());
        assertTrue(grafica.marcadores().isEmpty());
    }

    @Test
    @DisplayName("Porcentaje: el eje va de 0 a 100 y los puntos se distribuyen a lo ancho")
    void escalaPorcentaje() {
        GraficaTendencia grafica = GraficaTendencia.de(indicador(true, 80, 60,
                List.of(punto(100, EstadoSemaforo.VERDE), punto(0, EstadoSemaforo.ROJO))));

        assertFalse(grafica.vacia());
        assertEquals("14.0,12.0 306.0,108.0", grafica.puntos());
        assertEquals("31.2", grafica.yOptimo());
        assertEquals("50.4", grafica.yCritico());
        assertEquals("100", grafica.etiquetaMaximo());
        assertEquals("0", grafica.etiquetaMinimo());
        assertEquals("rojo", grafica.marcadores().get(1).claseCss());
    }

    @Test
    @DisplayName("Un solo punto se dibuja al centro")
    void unSoloPunto() {
        GraficaTendencia grafica = GraficaTendencia.de(indicador(false, 1, 3, List.of(punto(2, EstadoSemaforo.AMARILLO))));

        assertEquals("160.0", grafica.marcadores().get(0).x());
        assertTrue(grafica.marcadores().get(0).titulo().contains("Advertencia"));
    }

    @Test
    @DisplayName("Valores no porcentuales: la escala incluye los umbrales con 10 % de holgura")
    void escalaLibre() {
        GraficaTendencia grafica = GraficaTendencia.de(indicador(false, 1, 3,
                List.of(punto(2, EstadoSemaforo.AMARILLO), punto(4, EstadoSemaforo.ROJO))));

        assertEquals("0.7", grafica.etiquetaMinimo());
        assertEquals("4.3", grafica.etiquetaMaximo());
    }

    @Test
    @DisplayName("La escala no baja de 0 cuando no hay valores negativos")
    void escalaNoNegativa() {
        GraficaTendencia grafica = GraficaTendencia.de(indicador(false, 1, 3,
                List.of(punto(0.1, EstadoSemaforo.VERDE), punto(2, EstadoSemaforo.AMARILLO))));

        assertEquals("0", grafica.etiquetaMinimo());
        assertEquals("3.29", grafica.etiquetaMaximo());
    }
}
