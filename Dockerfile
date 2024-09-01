<<<<<<< HEAD
FROM amazoncorretto:17-alpine3.17
EXPOSE 8080
=======
FROM amazoncorretto:21-alpine3.17 AS builder

WORKDIR /app
COPY . .

RUN ./gradlew build --no-daemon

FROM amazoncorretto:21-alpine3.17
WORKDIR /app
COPY --from=builder app/app/build/libs/app-1.0-SNAPSHOT.jar /app/

RUN apk --no-cache add curl

EXPOSE 80
CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]
>>>>>>> dev
