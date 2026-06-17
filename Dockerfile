#Estagio 1: Construcao (Build) usando Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

#Copia o pom.xml e o codigo fonte
COPY pom.xml .
COPY src ./src

#Compila
RUN mvn clean package -DskipTests

#Estagio 2: Excecucao usando uma imagem JRE mais leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
#copia o .jar gerado no estagio anterior para este container
COPY --from=build /app/target/*.jar app.jar

#Expoe a porta padrao do spring boot
EXPOSE 8080

#Comando para iniciar io servidor Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
