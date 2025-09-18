# ===== Build frontend =====
FROM node:20-bullseye AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# ===== Build backend =====
FROM gradle:8.8-jdk17-alpine AS backend-build
WORKDIR /app
COPY backend/build.gradle backend/settings.gradle ./
RUN gradle --no-daemon dependencies || true
COPY backend/src ./src
RUN gradle --no-daemon clean bootJar

# ===== Run stage (monolith) =====
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
