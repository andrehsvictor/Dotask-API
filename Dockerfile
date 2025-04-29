FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY mvnw* ./
COPY .mvn .mvn/
COPY src src/
RUN mvn package -DskipTests -Dmaven.javadoc.skip=true -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]