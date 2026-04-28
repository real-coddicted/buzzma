FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin spring

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]