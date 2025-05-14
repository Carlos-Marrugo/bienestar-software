# Builder stage
FROM eclipse-temurin:21-jdk-jammy as builder

# Set encoding and locale
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build with explicit encoding
RUN ./mvnw package -DskipTests -Dfile.encoding=UTF-8

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy built jar from builder
COPY --from=builder /workspace/app/target/*.jar app.jar

# Set encoding and locale
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]