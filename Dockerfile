# Use an official OpenJDK runtime as a parent image
FROM maven:3.8.5-openjdk-17 AS build

# Set the Data Volume
VOLUME /root/.m2

# Set the working directory in the container
WORKDIR /home/app

# Expose port 8090
EXPOSE 8090

# Copy the Application source files and Maven project files (pom.xml)
COPY src ./src
COPY pom.xml ./

# Command to build the application JAR
RUN mvn -f ./pom.xml clean package -DskipTests

# Copy the application JAR
COPY ./target/*.jar app.jar

# Command to run the application
CMD ["java", "-jar", "app.jar"]
