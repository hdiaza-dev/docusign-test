# ===== Build frontend =====
FROM node:20-bullseye AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# ===== Build backend con GRADLE WRAPPER =====
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app

# Copiamos primero el wrapper y archivos de configuración para cachear deps
COPY backend/gradlew ./gradlew
COPY backend/gradle ./gradle
COPY backend/settings.gradle* ./        # soporta .gradle o .gradle.kts
COPY backend/build.gradle* ./           # soporta .gradle o .gradle.kts

# Permisos al wrapper
RUN chmod +x ./gradlew

# Descarga de dependencias (capa cacheable)
RUN ./gradlew --no-daemon --stacktrace --info build -x test || true

# Copiamos el código fuente
COPY backend/src ./src

# Build final del JAR (con logs detallados)
RUN ./gradlew --no-daemon --stacktrace --info clean bootJar

# ===== Run stage (monolito) =====
FROM eclipse-temurin:17-jre-alpine AS run
WORKDIR /app

# Carpetas para secretos y estáticos
RUN mkdir -p /app/config && chmod 700 /app/config && mkdir -p /app/static

# JAR del backend
COPY --from=backend-build /app/build/libs/*.jar /app/app.jar

# Estáticos del frontend
COPY --from=frontend-build /frontend/dist /app/static

# Render
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080

# JVM flags
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT} --spring.web.resources.static-locations=file:/app/static/"]
