
# Build stage
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Package stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/crypto-portfolio-tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
