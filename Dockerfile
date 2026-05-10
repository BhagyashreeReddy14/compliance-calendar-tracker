# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy POM first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests — tests run in CI, not in Docker image)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Bhagyashree Reddy"
LABEL description="Compliance Calendar Tracker — Spring Boot 3 Backend"

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create the uploads directory and set ownership
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser

# Expose the application port
EXPOSE 8080

# Use the production Spring profile
ENV SPRING_PROFILES_ACTIVE=prod

# JVM tuning for container environments
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
