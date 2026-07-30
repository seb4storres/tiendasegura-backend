# ---------- Stage 1: Build (Gradle, Groovy DSL) ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copiamos primero solo los archivos de build para cachear la resolución
# de dependencias en una capa separada del código fuente.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle gradle.properties ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon shadowJar -x test

# ---------- Stage 2: Runtime (JRE ligero) ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S tiendasegura && adduser -S tiendasegura -G tiendasegura
COPY --from=build /workspace/build/libs/*-all.jar app.jar
RUN chown tiendasegura:tiendasegura app.jar
USER tiendasegura

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
