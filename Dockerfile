FROM maven:3.8.3-openjdk-17 AS build
LABEL authors="yoankpatchavi"


WORKDIR /app
COPY . /app


COPY pom.xml /app
COPY src ./src




RUN mvn clean package -DskipTests


FROM openjdk:17-jdk-slim AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]