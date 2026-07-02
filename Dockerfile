<<<<<<< HEAD
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon -x test
RUN echo "=== JAR FILES ===" && ls -la /app/build/libs/

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
=======
FROM gradle:8.13-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle war --no-daemon

FROM tomcat:11.0-jdk21
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/build/libs/moonlight-backend-1.0.0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
>>>>>>> 4d3541e4495c2ff8c297be1e1aea1b33670576e0
