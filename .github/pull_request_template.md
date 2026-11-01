## Descripción

<!-- Qué cambia y por qué. -->

**Requisito / defecto relacionado:** RS-__ · CU__ · Closes #__

## Tipo de cambio

- [ ] Nueva funcionalidad
- [ ] Corrección de defecto
- [ ] Pruebas, documentación o refactorización

## Lista de verificación de la revisión (IEEE 1028)

### Autor
- [ ] El cambio está ligado a un Issue (requisito RS-xx o defecto)
- [ ] `mvn verify` pasa en mi máquina (pruebas en verde y cobertura ≥ 70 %)
- [ ] Agregué o actualicé pruebas con criterios Dado–Cuando–Entonces
- [ ] Respeta la arquitectura en capas (`ArquitecturaCapasTest` en verde)
- [ ] No subo archivos generados (`target/`, `data/`) ni credenciales

### Revisor (QA / Líder técnico)
- [ ] El código cumple el requisito y sus flujos alternos del SRS
- [ ] Las reglas de negocio están en el dominio y no en los controladores
- [ ] Nombres claros, sin código duplicado ni comentado
- [ ] Validaciones y mensajes de error comprensibles para el usuario
- [ ] El CI (Quality Gate) está en verde

## Evidencia

<!-- Capturas de pantalla, enlace al artefacto "evidencia-calidad" del CI, etc. -->
