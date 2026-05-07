FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN javac -cp "*" movie.java
CMD ["java", "-cp", ".:*", "movie"]
