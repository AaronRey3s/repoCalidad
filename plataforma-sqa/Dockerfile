FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ===== Etapa 2: Runtime (corregida) =====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Crear usuario de sistema sin privilegios (sin home, sin shell)
RUN useradd --system --uid 10001 --no-create-home appuser

# Crear directorios y asignar propietario ANTES de cambiar de usuario
RUN mkdir -p /app/data/evidencias \
    && chown -R appuser:appuser /app

# Copiar el jar con el propietario correcto
COPY --from=build --chown=appuser:appuser /build/target/plataforma-sqa-0.2.0-SNAPSHOT.jar app.jar

EXPOSE 8080

# Cambiar al usuario sin privilegios (antes del ENTRYPOINT)
USER 10001

ENTRYPOINT ["java", "-jar", "app.jar"]