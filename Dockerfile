# syntax=docker/dockerfile:1
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
FROM eclipse-temurin:25-jdk-alpine AS builder

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
COPY service/build.gradle.kts service/build.gradle.kts
COPY api-legacy/build.gradle.kts api-legacy/build.gradle.kts
COPY api-web/build.gradle.kts api-web/build.gradle.kts
COPY api-external/build.gradle.kts api-external/build.gradle.kts
COPY api-university/build.gradle.kts api-university/build.gradle.kts
COPY app/build.gradle.kts app/build.gradle.kts

# Download dependencies. --mount=type=cache persists ~/.gradle (deps + wrapper dist)
# ACROSS builds, so a build-file change re-resolves fast instead of re-downloading all.
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon || true

# Copy source code
COPY common/src common/src
COPY domain/src domain/src
COPY security/src security/src
COPY service/src service/src
COPY api-legacy/src api-legacy/src
COPY api-web/src api-web/src
COPY api-external/src api-external/src
COPY api-university/src api-university/src
COPY app/src app/src

# Build application (skip tests). Same gradle cache mount → reused deps + build outputs.
RUN --mount=type=cache,target=/root/.gradle ./gradlew :app:bootJar -x test --no-daemon

# =====================================================
# Runtime Stage
# =====================================================
FROM eclipse-temurin:25-jre-alpine

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
# --add-opens flags: Lombok 1.18.46 + Java 25 sun.misc.Unsafe.objectFieldOffset
# deprecation WARNING'ni o'chiradi. Module boundary'ni explicit ochib JVM warning
# logikasini chetlab o'tadi. Lombok upstream MethodHandles.Lookup'ga
# o'tmaguncha vaqtinchalik flag.
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"

# Run application with environment variables support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
