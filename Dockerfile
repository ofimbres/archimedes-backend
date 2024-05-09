FROM amazoncorretto:17-alpine3.17

# Set the working directory inside the container
WORKDIR /app
# Copy the JAR file from your project into the container
COPY app/build/libs/app-1.0-SNAPSHOT.jar /app/
# Expose the port your microservice listens on (if applicable)
EXPOSE 8081
# Define the command to run your microservice
CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]