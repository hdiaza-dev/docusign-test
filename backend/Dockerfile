# ===== Build stage =====
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copia sólo archivos necesarios para cachear dependencias
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon dependencies || true

# Copia el resto del código
COPY src src

# Compila (jar en build/libs)
RUN ./gradlew --no-daemon clean bootJar

# ===== Run stage =====
FROM eclipse-temurin:17-jre-jammy AS run
WORKDIR /app

# Copia el JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Render inyecta PORT; usa 8080 por defecto
ENV PORT=8080
EXPOSE 8080

# Opcional: afina memoria para instancias pequeñas de Render
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# Variables DocuSign (las definirás en el panel de Render)
# ENV DOCUSIGN_BASEPATH=https://demo.docusign.net/restapi
# ENV DOCUSIGN_AUTHSERVER=account-d.docusign.com
# ENV DOCUSIGN_CLIENTID=...
# ENV DOCUSIGN_USERID=...
# ENV DOCUSIGN_ACCOUNTID=...

# Si necesitas private_key.pem, puedes montarlo como secret file en /app/config
# y referenciarlo en application.properties: docusign.privateKeyFilename=/app/config/private_key.pem

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
