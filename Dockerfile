# Root Dockerfile for Railway/Render deployment - builds backend Spring Boot app
# This allows Railway to auto-detect and build the Docker image

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
COPY backend/src src

RUN ./mvnw clean package -DskipTests

# Use standard eclipse-temurin JRE for runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose port (Railway will override)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.launch.PropertiesLauncher --server.port=${PORT:-8080} || exit 1

# Start application - use exec form (no shell)
ENTRYPOINT ["java", "-jar", "app.jar"]
