# Matriz de pruebas de aceptación (ISO/IEC/IEEE 29119)

Las pruebas automatizadas viven en `plataforma-sqa/src/test/java`. La columna **Resultado** se llena
después de ejecutar `mvn verify` (o con el resultado del CI) en cada punto de control.

| Caso de uso | Escenario (Dado–Cuando–Entonces) | Técnica | Prueba automatizada | Resultado | Fecha |
|---|---|---|---|---|---|
| CU01 | Registrar proyecto válido vinculado a periodo | Partición de equivalencia | `ProyectoServiceTest.registraProyecto`, `FlujoWebIntegracionTest.registraProyecto` | | |
| CU01 – 3a | Fecha de fin anterior al inicio → se resalta `fechaFin` | Valores límite | `PeriodoTest.rechazaFinAnteriorAInicio`, `FlujoWebIntegracionTest.fechasInvalidas` | | |
| CU01 – 3a | Campos obligatorios vacíos | Partición inválida | `FlujoWebIntegracionTest.camposObligatorios` | | |
| CU01 – 3b | Proyecto duplicado en el periodo | Partición inválida | `ProyectoServiceTest.rechazaDuplicado`, `FlujoWebIntegracionTest.proyectoDuplicado` | | |
| CU02 | Configurar métrica con umbrales válidos | Partición de equivalencia | `MetricaServiceTest.configuraMetrica` | | |
| CU02 – 3a | Umbrales cruzados o iguales | Valores límite | `UmbralesTest.rechazaCruce*`, `UmbralesTest.rechazaLimitesIguales` | | |
| CU03 / RS-05 | Semáforo Verde/Amarillo/Rojo en los límites | Valores límite | `UmbralesTest.evaluaMayorEsMejor`, `UmbralesTest.evaluaMenorEsMejor` | | |
| CU03 – 2a | Valor no numérico o porcentaje fuera de [0,100] | Partición inválida | `ValorNumericoTest`, `MedicionServiceTest.rechaza*`, `FlujoWebIntegracionTest.registraMedicion` | | |
| CU04 | Cambio de estado con URL o archivo de evidencia | Partición de equivalencia | `TecnicaSqaServiceTest.aplicaConUrl`, `TecnicaSqaServiceTest.aplicaConArchivo` | | |
| CU04 – 3a | Marcar "Aplicado" sin evidencia → bloqueado | Partición inválida | `TecnicaSqaTest.bloqueaAplicadoSinEvidencia`, `FlujoWebIntegracionTest.gestionaTecnica` | | |
| CU05 | Dashboard con semáforos, tendencia y % de técnicas | Caja negra | `DashboardServiceTest.construyeDashboard`, `FlujoWebIntegracionTest.dashboardYReporte` | | |
| CU05 – 2a | Proyecto sin mediciones → "Sin datos" | Partición inválida | `DashboardServiceTest.sinMediciones` | | |
| CU06 | Exportar reporte PDF | Caja negra | `ReporteServiceTest.exportaPdf`, `GeneradorReportePdfTest` | | |
| CU06 – 2a | Sin registros en el periodo → se cancela | Partición inválida | `ReporteServiceTest.sinDatos`, `FlujoWebIntegracionTest.proyectoSinRegistros` | | |
| RNF-03 | Marca de tiempo y autor inmutables | Revisión + prueba | `MedicionServiceTest.registraMedicion`, `TecnicaSqaServiceTest.asignaTecnica` | | |
| RNF-06 | Arquitectura en capas | Análisis estático (ArchUnit) | `ArquitecturaCapasTest` | | |
| RNF-07 | Cobertura ≥ 70 % en dominio + servicio | JaCoCo (Quality Gate) | `mvn verify` (regla `quality-gate-cobertura`) | | |
