package mx.uaz.sqa.configuracion;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.Evidencia;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Periodo;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.Rol;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;
import mx.uaz.sqa.dominio.modelo.Usuario;
import mx.uaz.sqa.persistencia.CambioEstadoTecnicaRepository;
import mx.uaz.sqa.persistencia.MedicionRepository;
import mx.uaz.sqa.persistencia.MetricaRepository;
import mx.uaz.sqa.persistencia.ProyectoRepository;
import mx.uaz.sqa.persistencia.TecnicaSqaRepository;
import mx.uaz.sqa.persistencia.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial:
 * <ul>
 *   <li>Usuarios de prueba, uno por rol (siempre que la tabla de usuarios esté vacía).</li>
 *   <li>Un proyecto de demostración con métricas, mediciones y técnicas de EJEMPLO
 *       (solo si {@code sqa.demo.cargar-datos=true} y no hay proyectos). Los valores son
 *       ilustrativos: reemplácenlos con los datos reales de JaCoCo, GitHub Issues, etc.</li>
 * </ul>
 */
@Component
public class CargadorDatosIniciales implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargadorDatosIniciales.class);
    private static final String NOTA_DEMO = "Dato de ejemplo (demo)";

    private final UsuarioRepository usuarios;
    private final ProyectoRepository proyectos;
    private final MetricaRepository metricas;
    private final MedicionRepository mediciones;
    private final TecnicaSqaRepository tecnicas;
    private final CambioEstadoTecnicaRepository cambios;
    private final PasswordEncoder codificador;
    private final Clock reloj;
    private final boolean cargarDemo;

    public CargadorDatosIniciales(UsuarioRepository usuarios, ProyectoRepository proyectos,
                                  MetricaRepository metricas, MedicionRepository mediciones,
                                  TecnicaSqaRepository tecnicas, CambioEstadoTecnicaRepository cambios,
                                  PasswordEncoder codificador, Clock reloj,
                                  @Value("${sqa.demo.cargar-datos:false}") boolean cargarDemo) {
        this.usuarios = usuarios;
        this.proyectos = proyectos;
        this.metricas = metricas;
        this.mediciones = mediciones;
        this.tecnicas = tecnicas;
        this.cambios = cambios;
        this.codificador = codificador;
        this.reloj = reloj;
        this.cargarDemo = cargarDemo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() == 0) {
            crearUsuario("lider", "Líder de Proyecto (demo)", "lider123", Rol.LIDER_PROYECTO);
            crearUsuario("sqa", "Líder SQA (demo)", "sqa123", Rol.LIDER_SQA);
            crearUsuario("dev", "Desarrollador (demo)", "dev123", Rol.DESARROLLADOR);
            crearUsuario("stake", "Stakeholder (demo)", "stake123", Rol.STAKEHOLDER);
            LOG.info("Usuarios de prueba creados: lider/lider123, sqa/sqa123, dev/dev123, stake/stake123");
        }
        if (cargarDemo && proyectos.count() == 0) {
            cargarProyectoDemo();
            LOG.info("Proyecto de demostración cargado (sqa.demo.cargar-datos=true)");
        }
    }

    private void crearUsuario(String username, String nombre, String contrasena, Rol rol) {
        usuarios.save(new Usuario(username, nombre, codificador.encode(contrasena), rol));
    }

    private void cargarProyectoDemo() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDate hoy = ahora.toLocalDate();
        Proyecto proyecto = proyectos.save(new Proyecto(
                "Plataforma de Monitoreo SQA (demo)",
                "Proyecto de ejemplo: el equipo monitorea su propio desarrollo (dogfooding, sección 4.4 del SRS).",
                "Java 17, Spring Boot, Thymeleaf, H2, JUnit 5, JaCoCo",
                Periodo.de("Periodo demo", hoy.minusDays(60), hoy.plusDays(45)),
                ahora));

        Metrica cobertura = metricas.save(new Metrica(proyecto, "Cobertura de código (JaCoCo)",
                UnidadMedida.PORCENTAJE, Frecuencia.POR_SPRINT, SentidoMetrica.MAYOR_ES_MEJOR, 70.0, 50.0));
        Metrica exitoPruebas = metricas.save(new Metrica(proyecto, "Tasa de éxito de pruebas",
                UnidadMedida.PORCENTAJE, Frecuencia.POR_SPRINT, SentidoMetrica.MAYOR_ES_MEJOR, 95.0, 80.0));
        Metrica densidad = metricas.save(new Metrica(proyecto, "Densidad de defectos (defectos por CU)",
                UnidadMedida.RAZON, Frecuencia.SEMANAL, SentidoMetrica.MENOR_ES_MEJOR, 0.5, 1.5));
        Metrica criticos = metricas.save(new Metrica(proyecto, "Defectos críticos abiertos",
                UnidadMedida.CONTEO, Frecuencia.SEMANAL, SentidoMetrica.MENOR_ES_MEJOR, 0.0, 2.0));
        metricas.save(new Metrica(proyecto, "Complejidad ciclomática promedio",
                UnidadMedida.RAZON, Frecuencia.SEMANAL, SentidoMetrica.MENOR_ES_MEJOR, 5.0, 10.0));

        medir(cobertura, ahora, 21, 38.0, 14, 55.0, 3, 72.0);
        medir(exitoPruebas, ahora, 21, 78.0, 14, 88.0, 3, 96.0);
        medir(densidad, ahora, 21, 2.0, 14, 1.6, 3, 1.2);
        medir(criticos, ahora, 21, 5.0, 14, 4.0, 3, 3.0);

        tecnica(proyecto, "Revisión por pares de Pull Requests (IEEE 1028)", CategoriaTecnica.REVISION_TECNICA_PARES,
                "sqa", EstadoTecnica.APLICADO, "https://github.com/riagalv/repoCalidad/pulls", ahora);
        tecnica(proyecto, "Pruebas unitarias automatizadas (JUnit 5 + JaCoCo)", CategoriaTecnica.PRUEBAS_SOFTWARE,
                "dev", EstadoTecnica.EN_PROGRESO, null, ahora);
        tecnica(proyecto, "Análisis estático (SonarCloud)", CategoriaTecnica.ANALISIS_ESTATICO,
                "sqa", EstadoTecnica.PENDIENTE, null, ahora);
        tecnica(proyecto, "Auditoría de proceso (IEEE 730)", CategoriaTecnica.AUDITORIA_PROCESO,
                "lider", EstadoTecnica.PENDIENTE, null, ahora);
    }

    /** Registra pares (díasAtrás, valor) como mediciones de ejemplo. */
    private void medir(Metrica metrica, LocalDateTime ahora, double... diasYValores) {
        for (int i = 0; i + 1 < diasYValores.length; i += 2) {
            LocalDateTime instante = ahora.minusDays((long) diasYValores[i]);
            mediciones.save(metrica.registrarMedicion(diasYValores[i + 1], NOTA_DEMO, "dev", instante));
        }
        metricas.save(metrica);
    }

    private void tecnica(Proyecto proyecto, String nombre, CategoriaTecnica categoria, String responsable,
                         EstadoTecnica estado, String url, LocalDateTime ahora) {
        TecnicaSqa tecnica = tecnicas.save(new TecnicaSqa(proyecto, nombre, categoria, NOTA_DEMO, responsable));
        cambios.save(tecnica.registrarAsignacion("sqa", ahora.minusDays(20)));
        if (estado != EstadoTecnica.PENDIENTE) {
            cambios.save(tecnica.cambiarEstado(estado, Evidencia.deUrl(url), responsable, ahora.minusDays(2)));
            tecnicas.save(tecnica);
        }
    }
}
