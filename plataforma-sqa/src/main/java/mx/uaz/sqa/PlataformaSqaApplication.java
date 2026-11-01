package mx.uaz.sqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la Plataforma de Monitoreo y Gestión SQA.
 *
 * <p>Arquitectura orientada a objetos en capas (RNF-06):</p>
 * <ul>
 *   <li>{@code presentacion}: controladores web, formularios y vistas Thymeleaf.</li>
 *   <li>{@code servicio}: casos de uso CU01–CU06 (lógica de aplicación).</li>
 *   <li>{@code dominio}: entidades y reglas de negocio puras (umbrales, semáforo, evidencias).</li>
 *   <li>{@code persistencia}: repositorios JPA y almacenamiento de archivos de evidencia.</li>
 * </ul>
 */
@SpringBootApplication
public class PlataformaSqaApplication {

    public static void main(String[] args) {
        // Hibernate y Mockito usan Byte Buddy. En JDK más nuevos que los que Byte Buddy
        // reconoce oficialmente (p. ej. JDK 25) esta bandera evita que falle el arranque.
        // En JDK 17/21 no tiene ningún efecto.
        if (System.getProperty("net.bytebuddy.experimental") == null) {
            System.setProperty("net.bytebuddy.experimental", "true");
        }
        SpringApplication.run(PlataformaSqaApplication.class, args);
    }
}
