# Root Dockerfile for Render deployment - builds backend Spring Boot app
# This allows Render to auto-detect and build the Docker image

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
COPY backend/src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
