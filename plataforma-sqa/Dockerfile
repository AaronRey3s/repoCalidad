FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copiar pom.xml y descargar dependencias primero (aprovecha caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y empaquetar (puedes quitar -DskipTests si quieres que el Dockerfile corra JaCoCo/ArchUnit al construir)
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución (JRE 17)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Crear directorios para persistencia de H2 y evidencias (RS-08)
RUN mkdir -p /app/data/evidencias

COPY --from=build /build/target/plataforma-sqa-0.2.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]