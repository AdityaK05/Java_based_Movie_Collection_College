FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
# We need to compile the java files in the cinema folder using the libraries
RUN find cinema -name "*.java" > sources.txt && javac -cp "lib/*" @sources.txt
CMD ["java", "-cp", ".:lib/*", "cinema.Main"]
