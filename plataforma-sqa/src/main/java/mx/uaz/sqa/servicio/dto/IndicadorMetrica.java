package mx.uaz.sqa.servicio.dto;

import java.util.List;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;

/**
 * Indicador tipo semáforo de una métrica en el dashboard (RS-09).
 */
public record IndicadorMetrica(Long id, String nombre, String unidad, String frecuencia,
                               EstadoSemaforo estado, String valorActual,
                               String rangoVerde, String rangoAmarillo, String rangoRojo,
                               double limiteOptimo, double limiteCritico, boolean esPorcentaje,
                               List<PuntoSerie> serie) {

    public static IndicadorMetrica desde(Metrica metrica, List<Medicion> mediciones) {
        List<PuntoSerie> serie = mediciones.stream()
                .map(m -> new PuntoSerie(m.getRegistradoEn(), m.getValor(), m.valorFormateado(), m.getEstado()))
                .toList();
        return new IndicadorMetrica(metrica.getId(), metrica.getNombre(), metrica.getUnidad().getEtiqueta(),
                metrica.getFrecuencia().getEtiqueta(), metrica.getEstadoActual(), metrica.valorActualFormateado(),
                metrica.rangoVerde(), metrica.rangoAmarillo(), metrica.rangoRojo(),
                metrica.getUmbrales().getLimiteOptimo(), metrica.getUmbrales().getLimiteCritico(),
                metrica.getUnidad() == UnidadMedida.PORCENTAJE, serie);
    }

    public boolean tieneDatos() {
        return !serie.isEmpty();
    }
}
