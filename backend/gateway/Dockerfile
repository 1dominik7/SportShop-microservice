
FROM eclipse-temurin:24-jdk AS build

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:24-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar gateway.jar

ENTRYPOINT ["java", "-jar", "gateway.jar"]
