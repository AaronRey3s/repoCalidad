# Evidencia de aseguramiento de la calidad (SRS §4.5)

Carpeta dedicada a los artefactos que demuestran la aplicación de SQA durante el desarrollo.

| Carpeta / archivo | Qué guardar | Cómo se obtiene |
|---|---|---|
| `cobertura/` | Reporte HTML de JaCoCo (captura y/o copia de `index.html`) | `mvn verify` → `plataforma-sqa/target/site/jacoco/index.html`, o el artefacto **evidencia-calidad** de GitHub Actions |
| `pruebas/` | Resultados de pruebas (`surefire-reports`) y capturas | `mvn verify` → `plataforma-sqa/target/surefire-reports/` |
| `sonar/` | Capturas del Quality Gate de SonarCloud | Panel de SonarCloud (cuando se configure) |
| `pull-requests/` | Enlaces o capturas de PR revisados con la lista de verificación | GitHub → Pull requests |
| `matriz-pruebas-aceptacion.md` | Matriz de ejecución de pruebas por caso de uso | Se actualiza en cada punto de control |
| `bitacora-sqa.md` | Bitácora de actividades SQA (entrega final) | Se llena durante todo el proyecto |
