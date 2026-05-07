FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
# Compile all java files directly
RUN javac -cp "lib/*" cinema/model/MovieData.java cinema/dao/MovieManager.java cinema/servlet/MovieServlet.java cinema/Main.java
CMD ["java", "-cp", ".:lib/*", "cinema.Main"]
