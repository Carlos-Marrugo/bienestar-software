FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace/app

# Configurar locale y encoding primero
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
# Añadir parámetros de encoding
RUN ./mvnw package -DskipTests -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]