# syntax=docker/dockerfile:1.7

# ----- Build stage -----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy only pom first to maximize dependency caching
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests package

# ----- Runtime stage -----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the builder stage
ARG JAR_FILE=target/*.jar
COPY --from=builder /workspace/${JAR_FILE} /app/app.jar

# Non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser -u 10001 appuser
USER appuser

# Expose Spring Boot port
EXPOSE 8080

# JVM tuning for containerized environments
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Use a minimal entrypoint
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
