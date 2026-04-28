FROM gradle:8.13-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle war --no-daemon

FROM tomcat:11.0-jdk21
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/build/libs/moonlight-backend-1.0.0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]