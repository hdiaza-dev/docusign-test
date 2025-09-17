# ===== Build stage =====
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /app

# Copiamos sólo lo necesario para cachear dependencias
COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies || true

# Copiamos el código
COPY src ./src

# Compilamos el JAR
RUN gradle --no-daemon clean bootJar

# ===== Run stage =====
FROM eclipse-temurin:17-jre-alpine AS run
WORKDIR /app

# Copiamos el artefacto
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Render asigna PORT
ENV PORT=8080
EXPOSE 8080

# Ajustes de memoria típicos para contenedor pequeño
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# Si tu private_key.pem va como secret file, monta en /app/config y
# apunta en application.properties: docusign.privateKeyFilename=/app/config/private_key.pem

CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
