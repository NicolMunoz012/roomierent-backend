# Etapa 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar archivos de dependencias
COPY pom.xml .
COPY src ./src

# Construir el proyecto (sin ejecutar tests para ser más rápido)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Variable de entorno para el perfil de Spring
ENV SPRING_PROFILES_ACTIVE=production

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]