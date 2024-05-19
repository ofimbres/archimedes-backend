FROM amazoncorretto:17-alpine3.17 AS builder

WORKDIR /app
COPY . .
RUN ./gradlew build

FROM amazoncorretto:17-alpine3.17
WORKDIR /app
COPY --from=builder app/app/build/libs/app-1.0-SNAPSHOT.jar /app/

EXPOSE 80
CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]