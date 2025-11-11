# =====================================================
# HEMIS Backend - Multi-stage Docker Build (Best Practice)
# =====================================================
# Stage 1: Build with Gradle
# Stage 2: Run with JRE
# Environment variables from .env file
# =====================================================

# =====================================================
# Build Stage
# =====================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle gradle
COPY gradlew.bat gradlew ./
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Make gradlew executable
RUN chmod +x gradlew

# Copy all module build files
COPY common/build.gradle.kts common/build.gradle.kts
COPY domain/build.gradle.kts domain/build.gradle.kts
COPY security/build.gradle.kts security/build.gradle.kts
COPY admin-api/build.gradle.kts admin-api/build.gradle.kts
COPY app/build.gradle.kts app/build.gradle.kts

# Download dependencies (cache this layer if build files don't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY common/src common/src
COPY domain/src domain/src
COPY security/src security/src
COPY admin-api/src admin-api/src
COPY app/src app/src

# Build application (skip tests for faster build)
RUN ./gradlew :app:bootJar -x test --no-daemon

# =====================================================
# Runtime Stage
# =====================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 hemis && \
    adduser -D -u 1001 -G hemis hemis

# Copy JAR from build stage
COPY --from=builder /app/app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R hemis:hemis /app

# Switch to non-root user
USER hemis

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization flags
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run application with environment variables support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
