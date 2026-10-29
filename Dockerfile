# ==============================================================================
# Multi-stage Dockerfile for NormCat Backend
# Base Runtime: Eclipse Temurin JDK/JRE 21 (Alpine)
# ==============================================================================

# Stage 1: Build Application with Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Production Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable jar from builder stage
COPY --from=builder /build/target/normcat-0.0.1-SNAPSHOT.jar app.jar

# Configuration defaults
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=dev \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=normcat_db \
    DB_USER=postgres \
    DB_PASS=postgres

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
