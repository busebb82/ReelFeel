# 1. aşama: projeyi derle
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q package -DskipTests

# 2. aşama: sadece çalıştırmak için gereken daha küçük imaj
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/reelfeel-1.0.jar app.jar
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
