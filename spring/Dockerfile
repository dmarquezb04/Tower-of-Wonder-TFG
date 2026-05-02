# =============================================================================
# Dockerfile — Spring Boot Backend (Tower of Wonder)
# Multi-stage build: Maven oficial en Docker (no necesitas Maven local).
# =============================================================================

# =============================================================================
# ETAPA 1: BUILD
# Imagen oficial maven:3.9-eclipse-temurin-21 — incluye JDK 21 + Maven 3.9.
# Esta etapa compila el proyecto y genera el JAR ejecutable.
# Solo existe durante el build — no forma parte de la imagen final.
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar primero solo pom.xml para aprovechar el caché de capas Docker:
# Si pom.xml no cambia, las dependencias no se vuelven a descargar.
COPY pom.xml .

# Descargar dependencias (se cachean en una capa separada)
# -B: modo batch (sin interacción)
RUN mvn dependency:go-offline -B

# Ahora copiar el código fuente
COPY src ./src

# Compilar y empaquetar el JAR ejecutable
# -DskipTests: los tests se ejecutan en CI/CD, no en cada build
RUN mvn package -DskipTests -B

# =============================================================================
# ETAPA 2: RUNTIME
# Imagen ligera con solo el JRE (Java Runtime Environment).
# No incluye JDK ni Maven — imagen más pequeña y más segura.
# ~200MB vs ~600MB con JDK completo
# =============================================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar SOLO el JAR generado en la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto interno del contenedor (docker-compose.yml lo mapea al 8090 del host)
EXPOSE 8080

# Comando de arranque
# -Djava.security.egd: mejora el arranque en Linux (más entropía para SecureRandom)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
