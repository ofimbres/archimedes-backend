FROM amazoncorretto:21-alpine3.17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

FROM amazoncorretto:21-alpine3.17
WORKDIR /app
COPY --from=builder app/app/build/libs/app-1.0-SNAPSHOT.jar /app/
RUN apk --no-cache add curl

# ENV JAVA_OPTS="-Xms384m -Xmx768m -XX:+UseG1GC"

EXPOSE 80
# CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]

# HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
#     CMD curl -f http://localhost:80/actuator/health /proc/1/fd/1 2>&1 || exit 1

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:80/healthcheck/ || exit 1

# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app-1.0-SNAPSHOT.jar"]

CMD ["java", "-jar", "app-1.0-SNAPSHOT.jar"]
