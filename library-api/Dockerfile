# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy parent pom and module poms
COPY pom.xml .
COPY library-api/pom.xml library-api/
COPY library-business/pom.xml library-business/

# Copy source code
COPY library-api/src library-api/src
COPY library-business/src library-business/src

# Build the whole project
RUN mvn -B clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/library-api/target/library-api-*-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
