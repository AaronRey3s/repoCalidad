package mx.uaz.sqa.configuracion;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans generales de la aplicación.
 */
@Configuration
public class ConfiguracionGeneral {

    /**
     * Reloj del sistema. Se inyecta en los servicios (en lugar de usar LocalDateTime.now() directo)
     * para que las pruebas puedan fijar la hora y verificar las marcas de tiempo (RNF-03).
     */
    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
