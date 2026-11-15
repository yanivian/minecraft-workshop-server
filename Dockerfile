# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /build/target/minecraft-workshop-server-*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
