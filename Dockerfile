# Step 1: Use the Maven image with OpenJDK 17 to build the app
FROM maven:3.8.6-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy only pom.xml to cache dependencies separately
COPY pom.xml .

# Download dependencies to cache them
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Run Maven build to clean and package the application with debug logging
RUN mvn clean package -DskipTests -X

# Step 2: Use OpenJDK image to run the app
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/weatherWebApp-1.0.0.jar weather-app.jar

# Expose port 8080
EXPOSE 8080

# Run the app with Java
CMD ["java", "-jar", "weather-app.jar"]