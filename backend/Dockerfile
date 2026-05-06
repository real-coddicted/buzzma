FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Add security layer
RUN useradd --create-home --shell /usr/sbin/nologin spring

# Copy the JAR already built by the GitHub Action runner
COPY build/libs/*.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]