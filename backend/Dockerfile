# ===== Build stage =====
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /app

# Cache de dependencias
COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies || true

# Código
COPY src ./src

# Artefacto
RUN gradle --no-daemon clean bootJar

# ===== Run stage =====
FROM eclipse-temurin:17-jre-alpine AS run
WORKDIR /app

# Carpeta para secret file (PEM)
RUN mkdir -p /app/config && chmod 700 /app/config

# Copiamos el jar
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Render inyecta PORT
ENV PORT=8080
EXPOSE 8080

# JVM flags típicos para contenedores pequeños
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
