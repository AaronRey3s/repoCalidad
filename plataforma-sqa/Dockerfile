FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Se crea un usuario sin privilegios para ejecutar la aplicación
RUN groupadd -r sqagroup && useradd -r -g sqagroup -d /app -s /sbin/nologin sqauser \
    && mkdir -p /app/data/evidencias \
    && chown -R sqauser:sqagroup /app

COPY --from=build --chown=sqauser:sqagroup /build/target/plataforma-sqa-0.2.0-SNAPSHOT.jar app.jar

USER sqauser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]