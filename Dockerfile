FROM amazoncorretto:21-alpine3.17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

FROM amazoncorretto:21-alpine3.17
WORKDIR /app
COPY --from=builder app/app/build/libs/app-1.0-SNAPSHOT.jar /app/
RUN apk --no-cache add curl

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

EXPOSE 80
# CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 CMD curl -f http://localhost:80/actuator/health || exit 1 # Run the application

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app-1.0-SNAPSHOT.jar"]