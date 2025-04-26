FROM eclipse-temurin:21-jdk-alpine
LABEL author="Mileto Almeida"
WORKDIR /app

COPY target/*.jar app.jar

# Usa variáveis de ambiente definidas no docker-compose/.env
ENTRYPOINT ["java", "-jar", "app.jar"]
