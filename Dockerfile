FROM tomcat:11.0-jdk21

RUN rm -rf /usr/local/tomcat/webapps/*

COPY build/libs/moonlight-backend-1.0.0.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]