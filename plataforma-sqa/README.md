# Plataforma de Monitoreo y Gestión SQA

Aplicación web (Java 17 + Spring Boot 3) que implementa el SRS v1.2 del equipo: registro de proyectos por
periodo, métricas de calidad con semáforo, técnicas SQA con evidencias, dashboard de salud y reporte PDF.

## Requisitos para correrla

- **JDK 17** (también funciona con 21). Si tienes varios JDK instalados, selecciona el 17 en el IDE.
- **Maven 3.9+** o **IntelliJ IDEA** (trae Maven integrado). La primera vez se descargan las dependencias, así que se necesita internet.

## Cómo ejecutarla

**IntelliJ IDEA:** `File → Open…` → selecciona la carpeta `plataforma-sqa` (o su `pom.xml`) → espera a que
termine de importar Maven → ejecuta `PlataformaSqaApplication`.

**Terminal:**

```bash
cd plataforma-sqa
mvn spring-boot:run
```

Abre http://localhost:8080 e inicia sesión con alguno de los usuarios de prueba:

| Rol (Tabla 4 del SRS) | Usuario | Contraseña | Puede |
|---|---|---|---|
| Líder de Proyecto | `lider` | `lider123` | CU01 registrar/editar/desactivar proyectos, CU05, CU06 |
| Ingeniero / Líder SQA | `sqa` | `sqa123` | CU02 métricas, CU03 mediciones, CU04 técnicas y evidencias, CU05, CU06 |
| Desarrollador | `dev` | `dev123` | CU03 mediciones, CU04 actualizar técnicas, CU05 |
| Stakeholder | `stake` | `stake123` | CU05 dashboard, CU06 exportar reporte |

La base de datos es **H2 en archivo** (`./data/sqa-db`), no hay que instalar nada. Al primer arranque se carga un
proyecto de **demostración con valores de ejemplo** (no son mediciones reales). Para empezar en blanco pon
`sqa.demo.cargar-datos=false` en `application.properties` y borra la carpeta `data/`.
Para usar MySQL: ajusta `application-mysql.properties` y ejecuta con `--spring.profiles.active=mysql`.

## Pruebas y Quality Gate

```bash
mvn verify
```

Ejecuta, en este orden de la pirámide de pruebas (Tabla 10 del SRS):

1. **Unitarias** (JUnit 5 + Mockito) del dominio y los servicios, con nombres Dado–Cuando–Entonces.
2. **Integración** (Spring Boot Test + MockMvc + H2 en memoria): controladores, seguridad, JPA y plantillas.
3. **Arquitectura** (ArchUnit): verifica automáticamente las capas de RNF-06.
4. **Cobertura** (JaCoCo): el build **falla** si la cobertura de líneas de `dominio` + `servicio` es menor a 70 % (RNF-07).

Reportes: `target/site/jacoco/index.html` (cobertura) y `target/surefire-reports/` (resultados).
En GitHub Actions (`.github/workflows/ci.yml`) se ejecuta lo mismo en cada Pull Request y los reportes quedan
como artefacto `evidencia-calidad`.

## Arquitectura en capas (RNF-06)

```
mx.uaz.sqa
├── presentacion     Controladores, formularios, vistas (Thymeleaf) y gráfica SVG
├── servicio         Casos de uso CU01–CU06, DTOs y generación del PDF
├── dominio          Entidades y reglas de negocio puras (sin Spring)
│   ├── modelo       Proyecto, Periodo, Metrica, Umbrales, Medicion, TecnicaSqa, Evidencia…
│   └── excepcion    Una excepción por regla/flujo alterno del SRS
├── persistencia     Repositorios Spring Data JPA y almacenamiento de evidencias
└── configuracion    Seguridad, reloj del sistema y datos iniciales
```

Uso de POO: herencia (`EntidadPersistente → RegistroAuditable → Medicion / CambioEstadoTecnica`,
`ReglaNegocioException` y sus subclases, `ControladorBase`), polimorfismo (`SentidoMetrica` y `UnidadMedida`
evalúan/validan cada uno a su manera; `GeneradorReporte` permite otros formatos además de PDF) y encapsulamiento
(las entidades no tienen setters públicos; los cambios pasan por métodos que validan las reglas).

## Trazabilidad requisito → código → prueba

| Requisito | Dónde se implementa | Pruebas |
|---|---|---|
| RS-01 / RS-02 (CU01) | `Proyecto`, `Periodo`, `ProyectoService`, `ProyectoController` | `ProyectoTest`, `PeriodoTest`, `ProyectoServiceTest`, `FlujoWebIntegracionTest` |
| RS-03 (CU02) | `Metrica`, `Umbrales`, `SentidoMetrica`, `MetricaService` | `UmbralesTest`, `MetricaTest`, `MetricaServiceTest` |
| RS-04 (CU03) | `Metrica.registrarMedicion`, `Medicion`, `ValorNumerico`, `MedicionService` | `ValorNumericoTest`, `MetricaTest`, `MedicionServiceTest` |
| RS-05 | `Umbrales.evaluar`, `SentidoMetrica` | `UmbralesTest` (valores límite) |
| RS-06 / RS-07 (CU04) | `TecnicaSqa`, `CambioEstadoTecnica`, `TecnicaSqaService` | `TecnicaSqaTest`, `TecnicaSqaServiceTest` |
| RS-08 | `TecnicaSqa.cambiarEstado`, `Evidencia`, `AlmacenArchivosLocal` | `TecnicaSqaTest`, `EvidenciaTest`, `AlmacenArchivosLocalTest` |
| RS-09 (CU05) | `DashboardService`, `CalculadoraSalud`, `GraficaTendencia`, `dashboard/ver.html` | `DashboardServiceTest`, `CalculadoraSaludTest`, `GraficaTendenciaTest` |
| RS-10 / RS-11 (CU06) | `ReporteService`, `GeneradorReportePdf` | `ReporteServiceTest`, `GeneradorReportePdfTest` |
| RNF-01 | Tras iniciar sesión se llega a Proyectos; el dashboard está a 1 clic | Prueba manual |
| RNF-02 | `EstadoSemaforo` + `app.css` (verde/amarillo/rojo) | `CatalogosTest` |
| RNF-03 | `RegistroAuditable` (`updatable = false`), `@Immutable` en `Medicion` y `CambioEstadoTecnica` | `MedicionServiceTest`, `TecnicaSqaServiceTest` |
| RNF-06 | Paquetes por capa | `ArquitecturaCapasTest` |
| RNF-07 | Regla JaCoCo en `pom.xml` | `mvn verify` |

## Decisiones de diseño que el equipo debe validar

- **Umbrales con dos límites + sentido.** Cada métrica define un límite óptimo, un límite crítico y si "mayor es
  mejor" o "menor es mejor". Así los rangos Verde/Amarillo/Rojo siempre son continuos y sin huecos (CU02 paso 3);
  si los límites se cruzan o son iguales, se rechaza (CU02 flujo 3a).
- **Índice global y dictamen** (el SRS no define la fórmula): puntaje por métrica Verde 100 / Amarillo 50 / Rojo 0;
  índice global = promedio entre el índice de métricas y el % de técnicas en estado Aplicado; dictamen
  ≥ 80 Saludable, ≥ 60 En riesgo, < 60 Crítico. Se cambia en un solo lugar: `CalculadoraSalud`.
- **"Sin registros suficientes" (CU06 flujo 2a)** = el proyecto no tiene ninguna medición ni ninguna técnica.
- El histórico es inmutable: no se pueden editar ni borrar mediciones, y una métrica solo se puede eliminar si
  todavía no tiene mediciones.

## Pendiente (no cubierto todavía)

- RNF-04 (tiempo de respuesta) y RNF-05 (25 sesiones concurrentes) no tienen prueba automatizada; se sugiere una
  prueba de carga con JMeter antes de la versión Beta.
- Administración de usuarios desde la interfaz (hoy se crean al arrancar).
- SonarCloud: falta crear el proyecto en sonarcloud.io, poner `sonar.organization` y `sonar.projectKey` en el
  `pom.xml` y agregar el secreto `SONAR_TOKEN` al repositorio.
