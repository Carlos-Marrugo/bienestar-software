FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace/app

# First copy just the POM and install dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Then copy everything else and build
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]