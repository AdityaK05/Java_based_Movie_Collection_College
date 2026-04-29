FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY . .
CMD ["java", "-cp", "cinema.jar:jetty-server.jar:jetty-servlet.jar:jetty-util.jar:jetty-http.jar:jetty-io.jar:jetty-security.jar:servlet-api.jar:.", "movie"]
