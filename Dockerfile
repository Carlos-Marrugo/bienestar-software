FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace/app
COPY . .
RUN chmod +x mvnw  # Add this line
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]