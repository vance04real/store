# Multi-stage Dockerfile for Store Application
# ===========================================

# STAGE 1: Build Stage with OpenAPI Generation
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files (for caching)
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# Copy OpenAPI specification FIRST (needed for generation)
COPY src/main/resources/specification/ src/main/resources/specification/

# Make gradlew executable
RUN chmod +x gradlew

# CRITICAL: Generate OpenAPI classes BEFORE copying source code
RUN ./gradlew openApiGenerate --no-daemon

# Now copy source code (which depends on generated classes)
COPY src/ src/

# Build the application (now all classes are available)
RUN ./gradlew clean build -x test --no-daemon

# Verify JAR was created
RUN ls -la build/libs/ && echo "JAR file created successfully"

# STAGE 2: Runtime Stage (Lightweight)
FROM eclipse-temurin:17-jre-alpine AS runtime

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Metadata labels
LABEL maintainer="evanskfc"
LABEL version="1.0.0"
LABEL description="Store Application - Spring Boot REST API with Basic Auth"