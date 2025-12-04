# Use Eclipse Temurin JDK 17 as base image (matching build.gradle)
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x ./gradlew

# Run tests first
RUN ./gradlew test

# Build the application (tests already ran, so we can skip them)
RUN ./gradlew build -x test

# Create final runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar /app/discount-service.jar

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/hello || exit 1

# Set JVM options for container
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/discount-service.jar"]