
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar user.jar

ENTRYPOINT ["java", "-jar", "user.jar"]
