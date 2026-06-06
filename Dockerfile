# Use a multi-stage build to create a lean final image
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && \
    curl -L -o dd-java-agent.jar https://dtdg.co/latest-java-tracer
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-Ddd.agent.host=$(DD_AGENT_HOST)", "-Ddd.service=$(DD_SERVICE)", "-Ddd.env=$(DD_ENV)", "-Ddd.version=$(DD_VERSION)", "-Ddd.trace.otel.enabled=true", "-jar", "/app/app.jar"]
