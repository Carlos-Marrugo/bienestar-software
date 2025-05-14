FROM eclipse-temurin:21-jdk-jammy as builder

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

WORKDIR /workspace/app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY --chown=1000:1000 src src

RUN ./mvnw package -DskipTests \
    -Dfile.encoding=UTF-8 \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8 \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn