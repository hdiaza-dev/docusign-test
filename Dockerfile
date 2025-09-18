# ===== Build frontend =====
FROM node:20-bullseye AS frontend-build
WORKDIR /frontend
COPY frontend/ ./
RUN npm ci
RUN npm run build

# ===== Build backend con imagen de Gradle (sin wrapper) =====
FROM gradle:8.8-jdk17-alpine AS backend-build
WORKDIR /app
COPY backend/ ./
# Si no quieres ejecutar tests en el build, añade -x test
RUN gradle --no-daemon --stacktrace --info clean bootJar

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
