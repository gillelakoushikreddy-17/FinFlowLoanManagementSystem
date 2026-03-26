# Build the JAR locally first with: mvn clean package -DskipTests
# Then run: docker-compose up -d --build
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
