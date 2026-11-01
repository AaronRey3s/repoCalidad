package mx.uaz.sqa.servicio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import mx.uaz.sqa.dominio.excepcion.RecursoNoEncontradoException;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.ValorNumerico;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU03 – Registrar Medición de Métrica (RS-04, RS-05).
 */
@Service
@Transactional
public class MedicionService {

    private final MetricaRepository metricas;
    private final MedicionRepository mediciones;
    private final Clock reloj;

    public MedicionService(MetricaRepository metricas, MedicionRepository mediciones, Clock reloj) {
        this.metricas = metricas;
        this.mediciones = mediciones;
        this.reloj = reloj;
    }

    /**
     * Registra el valor con fecha/hora actual y autor, y actualiza el semáforo de la métrica.
     *
     * @param valorTexto valor tal como lo capturó el usuario (se valida que sea numérico)
     */
    public Medicion registrar(Long metricaId, String valorTexto, String observacion, String autor) {
        Metrica metrica = metricas.findById(metricaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la métrica", metricaId));
        double valor = ValorNumerico.desdeTexto(valorTexto, "valor");
        Medicion medicion = metrica.registrarMedicion(valor, observacion, autor, LocalDateTime.now(reloj));
        return mediciones.save(medicion);
    }

    @Transactional(readOnly = true)
    public List<Medicion> historial(Long metricaId) {
        return mediciones.findByMetricaIdOrderByRegistradoEnAscIdAsc(metricaId);
    }
}
