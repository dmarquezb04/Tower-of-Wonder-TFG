# Etapa 1: Compilación y generación de Javadoc
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias (aprovechando la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y generamos el JAR y el Javadoc
COPY src ./src
RUN mvn clean package javadoc:javadoc -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Copiamos también el Javadoc generado para que WebConfig pueda servirlo
COPY --from=build /app/target/reports/apidocs ./target/reports/apidocs

# Exponemos el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
