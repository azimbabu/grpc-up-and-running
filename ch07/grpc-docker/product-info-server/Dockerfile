# using gradle 6.6 as the first Gradle version to support arm64 is v6.6
FROM gradle:6.6-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11.0.15-jre-slim

EXPOSE 50051

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/product-info-server.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/product-info-server.jar"]