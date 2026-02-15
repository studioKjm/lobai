# Multi-stage build for Spring Boot backend

# Stage 1: Build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle files first for caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application (skip tests for faster build)
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Create uploads directory and set permissions
RUN mkdir -p uploads && chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose ports
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set timezone to Asia/Seoul
ENV TZ=Asia/Seoul

# Run application with optimized JVM settings
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+UseStringDeduplication", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Duser.timezone=Asia/Seoul", \
  "-Dspring.profiles.active=docker", \
  "-jar", \
  "app.jar"]
