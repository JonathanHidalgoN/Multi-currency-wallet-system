# Documentation & Best Practices:
#https://snyk.io/blog/best-practices-to-build-java-containers-with-docker/
# ============================================

FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy only pom.xml first (Docker layer caching optimization)
# If pom.xml doesn't change, Docker reuses this layer and doesn't re-download dependencies
COPY pom.xml .

# Download dependencies (this layer is cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

COPY src ./src

# Build the application and create executable JAR
# -DskipTests: skip tests during build (run them separately in CI/CD)
# -B: batch mode (non-interactive)
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime Stage
# ============================================
# Use smaller JRE-only image for running the application
FROM eclipse-temurin:21-jre-alpine

# Add metadata labels
LABEL maintainer="payflow-team"
LABEL description="PayFlow Wallet API - Multi-Currency Wallet System"

# Create a non-root user for security
# Running as root in containers is a security risk
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the JAR file from builder stage
# --chown: set ownership to spring user
COPY --from=builder --chown=spring:spring /app/target/wallet-api-1.0-SNAPSHOT.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose port 8080 (documentation only, doesn't actually publish the port)
EXPOSE 8080

# Health check: verify the application is responding via Spring Boot Actuator
# Note: context-path is /api, so health endpoint is at /api/actuator/health
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Run the application
# -XX:+UseContainerSupport: JVM respects container memory limits
# -Djava.security.egd: faster startup (use non-blocking random)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
