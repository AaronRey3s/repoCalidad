package mx.uaz.sqa.soporte;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.dominio.modelo.EntidadPersistente;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Periodo;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;

/**
 * Objetos de prueba reutilizables (patrón Object Mother).
 */
public final class Fabrica {

    public static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 30);
    public static final LocalDate INICIO_PERIODO = LocalDate.of(2026, 8, 3);
    public static final LocalDate FIN_PERIODO = LocalDate.of(2026, 12, 11);

    private Fabrica() {
    }

    public static Clock reloj() {
        return Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
    }

    public static Periodo periodo() {
        return Periodo.de("Ago-Dic 2026", INICIO_PERIODO, FIN_PERIODO);
    }

    public static Proyecto proyecto() {
        return proyecto("Plataforma SQA");
    }

    public static Proyecto proyecto(String nombre) {
        return new Proyecto(nombre, "Proyecto de prueba", "Java, Spring Boot", periodo(), AHORA);
    }

    /** Porcentaje, mayor es mejor: Verde ≥ 80, Amarillo ≥ 60 y < 80, Rojo < 60. */
    public static Metrica metricaCobertura(Proyecto proyecto) {
        return new Metrica(proyecto, "Cobertura de código", UnidadMedida.PORCENTAJE, Frecuencia.POR_SPRINT,
                SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0);
    }

    /** Razón, menor es mejor: Verde ≤ 1, Amarillo > 1 y ≤ 3, Rojo > 3. */
    public static Metrica metricaDefectos(Proyecto proyecto) {
        return new Metrica(proyecto, "Densidad de defectos", UnidadMedida.RAZON, Frecuencia.SEMANAL,
                SentidoMetrica.MENOR_ES_MEJOR, 1.0, 3.0);
    }

    public static TecnicaSqa tecnica(Proyecto proyecto) {
        return new TecnicaSqa(proyecto, "Revisión por pares del CU01", CategoriaTecnica.REVISION_TECNICA_PARES,
                "Revisión con checklist IEEE 1028", "sqa");
    }

    /** Asigna un id como si la entidad ya estuviera persistida. */
    public static <T extends EntidadPersistente> T conId(T entidad, long id) {
        try {
            Field campo = EntidadPersistente.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(entidad, id);
            return entidad;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
