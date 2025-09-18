# ===== Build frontend =====
FROM node:20-bullseye AS frontend-build
WORKDIR /frontend
COPY frontend/ ./
RUN npm ci
RUN npm run build -- --mode dev

# ===== Build backend con imagen de Gradle (sin wrapper) =====
FROM gradle:8.8-jdk17 AS backend-build
WORKDIR /app
# Copiamos TODO el backend (incluye gradle.properties si lo tienes)
COPY backend/ ./

# Memoria extra para Gradle dentro del contenedor
ENV GRADLE_OPTS="-Xmx1024m -Dorg.gradle.daemon=false"

# Build del jar (omitimos tests para evitar transforms del árbol de test)
RUN gradle --no-daemon --stacktrace --info clean bootJar -x test

# ===== Run (monolito) =====
FROM eclipse-temurin:17-jre-alpine AS run
WORKDIR /app
RUN mkdir -p /app/config && chmod 700 /app/config && mkdir -p /app/static
COPY --from=backend-build /app/build/libs/*.jar /app/app.jar
COPY --from=frontend-build /frontend/dist /app/static
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT} --spring.web.resources.static-locations=file:/app/static/"]
