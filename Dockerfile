FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw* ./
COPY .mvn .mvn/
COPY src src/
RUN mvn package -DskipTests -Dmaven.javadoc.skip=true -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser
WORKDIR /app
RUN mkdir -p /app/logs && \
    chown -R javauser:javauser /app

# Create a directory for secrets and set permissions
# to read and execute for all users
RUN mkdir -p /etc/secrets && \
    chmod 755 /etc/secrets && \
    chown -R javauser:javauser /etc/secrets

COPY --from=build --chown=javauser:javauser /app/target/*.jar /app/app.jar
EXPOSE 8080
USER javauser
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]