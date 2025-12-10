# Multi-stage build optimized for Linux/AWS
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# Ensure gradlew has Unix line endings and is executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src/

# Build the application with tests
RUN ./gradlew clean build --no-daemon

# Create final runtime image (Linux-optimized, smaller size)
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar /app/discount-service.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/hello || exit 1

# Set JVM options for container (Linux optimized)
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/discount-service.jar"]