# Build the Spring Boot executable JAR with the project's Gradle wrapper.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew --no-daemon bootJar

# Use a smaller Java runtime image for Render.
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/app.jar /app/app.jar

# Render's web services use PORT (10000 by default).
ENV PORT=10000
EXPOSE 10000

# Bind on all interfaces and honor Render's assigned port.
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.address=0.0.0.0 --server.port=${PORT:-10000}"]
