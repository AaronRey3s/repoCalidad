package mx.uaz.sqa.presentacion;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mx.uaz.sqa.dominio.modelo.CategoriaTecnica;
import mx.uaz.sqa.dominio.modelo.EstadoSemaforo;
import mx.uaz.sqa.dominio.modelo.EstadoTecnica;
import mx.uaz.sqa.dominio.modelo.Frecuencia;
import mx.uaz.sqa.dominio.modelo.Medicion;
import mx.uaz.sqa.dominio.modelo.Metrica;
import mx.uaz.sqa.dominio.modelo.Proyecto;
import mx.uaz.sqa.dominio.modelo.SentidoMetrica;
import mx.uaz.sqa.dominio.modelo.TecnicaSqa;
import mx.uaz.sqa.dominio.modelo.UnidadMedida;
import mx.uaz.sqa.servicio.MedicionService;
import mx.uaz.sqa.servicio.MetricaService;
import mx.uaz.sqa.servicio.ProyectoService;
import mx.uaz.sqa.servicio.TecnicaSqaService;
import mx.uaz.sqa.servicio.dto.DatosMetrica;
import mx.uaz.sqa.servicio.dto.DatosProyecto;
import mx.uaz.sqa.servicio.dto.DatosTecnica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración (Tabla 10, nivel 2): controladores + servicios + JPA (H2 en memoria)
 * + seguridad + renderizado de plantillas Thymeleaf.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración web – CU01 a CU06")
class FlujoWebIntegracionTest {

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProyectoService proyectoService;
    @Autowired
    private MetricaService metricaService;
    @Autowired
    private MedicionService medicionService;
    @Autowired
    private TecnicaSqaService tecnicaService;

    private Proyecto nuevoProyecto() {
        return proyectoService.crear(new DatosProyecto("Proyecto IT " + SECUENCIA.incrementAndGet(),
                "Proyecto de integración", "Java", "Ago-Dic 2026",
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 12, 11)));
    }

    private Metrica nuevaMetrica(Long proyectoId) {
        return metricaService.configurar(proyectoId, new DatosMetrica("Cobertura", UnidadMedida.PORCENTAJE,
                Frecuencia.POR_SPRINT, SentidoMetrica.MAYOR_ES_MEJOR, 80.0, 60.0));
    }

    private TecnicaSqa nuevaTecnica(Long proyectoId) {
        return tecnicaService.asignar(proyectoId, new DatosTecnica("Revisión por pares",
                CategoriaTecnica.REVISION_TECNICA_PARES, null, "sqa"), "sqa");
    }

    // ---------- Autenticación ----------

    @Test
    @DisplayName("Un usuario anónimo es redirigido al inicio de sesión")
    void anonimoVaAlLogin() throws Exception {
        mvc.perform(get("/proyectos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("La página de inicio de sesión es pública")
    void loginPublico() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Los usuarios de prueba inician sesión con su rol; una contraseña incorrecta se rechaza")
    void inicioDeSesion() throws Exception {
        mvc.perform(formLogin("/login").user("lider").password("lider123"))
                .andExpect(authenticated().withUsername("lider").withRoles("LIDER_PROYECTO"))
                .andExpect(redirectedUrl("/proyectos"));

        mvc.perform(formLogin("/login").user("lider").password("incorrecta"))
                .andExpect(unauthenticated());
    }

    // ---------- CU01 ----------

    @Test
    @WithMockUser(username = "lider", roles = "LIDER_PROYECTO")
    @DisplayName("CU01: el Líder de Proyecto registra un proyecto y navega sus pantallas")
    void registraProyecto() throws Exception {
        mvc.perform(get("/proyectos/nuevo")).andExpect(status().isOk());

        mvc.perform(post("/proyectos").with(csrf())
                        .param("nombre", "Proyecto web " + SECUENCIA.incrementAndGet())
                        .param("etiquetaPeriodo", "Ago-Dic 2026")
                        .param("fechaInicio", "2026-08-03")
                        .param("fechaFin", "2026-12-11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/proyectos/*"));

        Proyecto proyecto = nuevoProyecto();
        mvc.perform(get("/proyectos")).andExpect(status().isOk()).andExpect(view().name("proyectos/lista"));
        mvc.perform(get("/proyectos/{id}", proyecto.getId())).andExpect(status().isOk());
        mvc.perform(get("/proyectos/{id}/editar", proyecto.getId())).andExpect(status().isOk());
        mvc.perform(post("/proyectos/{id}", proyecto.getId()).with(csrf())
                        .param("nombre", proyecto.getNombre() + " (editado)")
                        .param("etiquetaPeriodo", "Ago-Dic 2026")
                        .param("fechaInicio", "2026-08-03")
                        .param("fechaFin", "2026-12-11"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/proyectos/{id}/activo", proyecto.getId()).with(csrf()).param("activo", "false"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "LIDER_PROYECTO")
    @DisplayName("CU01 flujo 3a: fecha de fin anterior al inicio resalta el campo fechaFin")
    void fechasInvalidas() throws Exception {
        mvc.perform(post("/proyectos").with(csrf())
                        .param("nombre", "Fechas mal")
                        .param("etiquetaPeriodo", "Ago-Dic 2026")
                        .param("fechaInicio", "2026-12-11")
                        .param("fechaFin", "2026-08-03"))
                .andExpect(status().isOk())
                .andExpect(view().name("proyectos/formulario"))
                .andExpect(model().attributeHasFieldErrors("form", "fechaFin"));
    }

    @Test
    @WithMockUser(roles = "LIDER_PROYECTO")
    @DisplayName("CU01 flujo 3a: campos obligatorios vacíos se resaltan")
    void camposObligatorios() throws Exception {
        mvc.perform(post("/proyectos").with(csrf()).param("nombre", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "nombre", "etiquetaPeriodo", "fechaInicio", "fechaFin"));
    }

    @Test
    @WithMockUser(roles = "LIDER_PROYECTO")
    @DisplayName("CU01 flujo 3b: proyecto duplicado en el periodo")
    void proyectoDuplicado() throws Exception {
        Proyecto existente = nuevoProyecto();

        mvc.perform(post("/proyectos").with(csrf())
                        .param("nombre", existente.getNombre().toUpperCase())
                        .param("etiquetaPeriodo", "Ago-Dic 2026")
                        .param("fechaInicio", "2026-08-03")
                        .param("fechaFin", "2026-12-11"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "nombre"));
    }

    @Test
    @WithMockUser(roles = "STAKEHOLDER")
    @DisplayName("Seguridad: un Stakeholder no puede registrar proyectos ni métricas")
    void stakeholderSinPermisos() throws Exception {
        Proyecto proyecto = nuevoProyecto();

        mvc.perform(post("/proyectos").with(csrf()).param("nombre", "X"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/proyectos/{id}/metricas/nueva", proyecto.getId()))
                .andExpect(status().isForbidden());
    }

    // ---------- CU02 ----------

    @Test
    @WithMockUser(username = "sqa", roles = "LIDER_SQA")
    @DisplayName("CU02: configura una métrica y rechaza umbrales cruzados (flujo 3a)")
    void configuraMetrica() throws Exception {
        Proyecto proyecto = nuevoProyecto();
        mvc.perform(get("/proyectos/{id}/metricas/nueva", proyecto.getId())).andExpect(status().isOk());

        mvc.perform(post("/proyectos/{id}/metricas", proyecto.getId()).with(csrf())
                        .param("nombre", "Cobertura")
                        .param("unidad", "PORCENTAJE")
                        .param("frecuencia", "SEMANAL")
                        .param("sentido", "MAYOR_ES_MEJOR")
                        .param("limiteOptimo", "80")
                        .param("limiteCritico", "60"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/proyectos/{id}/metricas", proyecto.getId()).with(csrf())
                        .param("nombre", "Tasa de éxito")
                        .param("unidad", "PORCENTAJE")
                        .param("frecuencia", "SEMANAL")
                        .param("sentido", "MAYOR_ES_MEJOR")
                        .param("limiteOptimo", "60")
                        .param("limiteCritico", "80"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "limiteCritico"));

        mvc.perform(post("/proyectos/{id}/metricas", proyecto.getId()).with(csrf())
                        .param("nombre", "Texto")
                        .param("unidad", "PORCENTAJE")
                        .param("frecuencia", "SEMANAL")
                        .param("sentido", "MAYOR_ES_MEJOR")
                        .param("limiteOptimo", "ochenta")
                        .param("limiteCritico", "60"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "limiteOptimo"));

        Metrica sinMediciones = metricaService.listarPorProyecto(proyecto.getId()).get(0);
        mvc.perform(post("/metricas/{id}/eliminar", sinMediciones.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- CU03 ----------

    @Test
    @WithMockUser(username = "dev", roles = "DESARROLLADOR")
    @DisplayName("CU03: registra una medición con autor y valida caracteres no numéricos (flujo 2a)")
    void registraMedicion() throws Exception {
        Proyecto proyecto = nuevoProyecto();
        Metrica metrica = nuevaMetrica(proyecto.getId());

        mvc.perform(get("/metricas/{id}/mediciones", metrica.getId())).andExpect(status().isOk());

        mvc.perform(post("/metricas/{id}/mediciones", metrica.getId()).with(csrf())
                        .param("valor", "85")
                        .param("observacion", "Sprint 2"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/metricas/{id}/mediciones", metrica.getId()).with(csrf()).param("valor", "abc"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "valor"));

        mvc.perform(post("/metricas/{id}/mediciones", metrica.getId()).with(csrf()).param("valor", "150"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "valor"));

        List<Medicion> historial = medicionService.historial(metrica.getId());
        assertEquals(1, historial.size());
        assertEquals("dev", historial.get(0).getAutor());
        assertEquals(EstadoSemaforo.VERDE, historial.get(0).getEstado());

        mvc.perform(get("/metricas/{id}/mediciones", metrica.getId())).andExpect(status().isOk());
    }

    // ---------- CU04 ----------

    @Test
    @WithMockUser(username = "sqa", roles = "LIDER_SQA")
    @DisplayName("CU04: asigna técnica, exige evidencia para 'Aplicado' (flujo 3a) y acepta un archivo")
    void gestionaTecnica() throws Exception {
        Proyecto proyecto = nuevoProyecto();
        mvc.perform(get("/proyectos/{id}/tecnicas/nueva", proyecto.getId())).andExpect(status().isOk());
        mvc.perform(post("/proyectos/{id}/tecnicas", proyecto.getId()).with(csrf())
                        .param("nombre", "Análisis estático")
                        .param("categoria", "ANALISIS_ESTATICO")
                        .param("responsable", "sqa"))
                .andExpect(status().is3xxRedirection());

        TecnicaSqa tecnica = nuevaTecnica(proyecto.getId());
        mvc.perform(get("/tecnicas/{id}", tecnica.getId())).andExpect(status().isOk());

        mvc.perform(multipart("/tecnicas/{id}/estado", tecnica.getId()).with(csrf())
                        .param("estado", "APLICADO")
                        .param("evidenciaUrl", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "evidenciaUrl"));

        byte[] contenido = "acta de revisión".getBytes(StandardCharsets.UTF_8);
        mvc.perform(multipart("/tecnicas/{id}/estado", tecnica.getId())
                        .file(new MockMultipartFile("archivo", "acta.pdf", "application/pdf", contenido))
                        .with(csrf())
                        .param("estado", "APLICADO"))
                .andExpect(status().is3xxRedirection());

        assertEquals(EstadoTecnica.APLICADO, tecnicaService.obtener(tecnica.getId()).getEstado());
        mvc.perform(get("/tecnicas/{id}/evidencia", tecnica.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(contenido));
        assertEquals(2, tecnicaService.historial(tecnica.getId()).size());
    }

    // ---------- CU05 y CU06 ----------

    @Test
    @WithMockUser(username = "stake", roles = "STAKEHOLDER")
    @DisplayName("CU05 y CU06: el Stakeholder consulta el dashboard y exporta el PDF")
    void dashboardYReporte() throws Exception {
        Proyecto proyecto = nuevoProyecto();
        Metrica metrica = nuevaMetrica(proyecto.getId());
        medicionService.registrar(metrica.getId(), "65", null, "dev");
        nuevaTecnica(proyecto.getId());

        mvc.perform(get("/dashboard")).andExpect(status().isOk()).andExpect(view().name("dashboard/ver"));
        mvc.perform(get("/dashboard").param("proyectoId", String.valueOf(proyecto.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("d", "tarjetas"));

        mvc.perform(get("/proyectos/{id}/reporte.pdf", proyecto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("reporte-salud-")));
    }

    @Test
    @WithMockUser(username = "sqa", roles = "LIDER_SQA")
    @DisplayName("CU05 flujo 2a y CU06 flujo 2a: proyecto sin registros")
    void proyectoSinRegistros() throws Exception {
        Proyecto proyecto = nuevoProyecto();

        mvc.perform(get("/dashboard").param("proyectoId", String.valueOf(proyecto.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("d"));

        mvc.perform(get("/proyectos/{id}/reporte.pdf", proyecto.getId()))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", containsString("reporteSinDatos")));
    }

    @Test
    @WithMockUser
    @DisplayName("Un recurso inexistente muestra la página 404")
    void recursoInexistente() throws Exception {
        mvc.perform(get("/proyectos/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }
}
