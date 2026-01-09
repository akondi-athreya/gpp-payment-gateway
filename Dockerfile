# Root Dockerfile for Railway/Render deployment - builds backend Spring Boot app
# This allows Railway to auto-detect and build the Docker image

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
COPY backend/src src

RUN ./mvnw clean package -DskipTests

# Use Ubuntu base instead of alpine for better runtime compatibility
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Default to Railway's dynamic port assignment
EXPOSE 8080

# Use shell form to allow env var substitution
CMD ["java", "-jar", "app.jar"]
