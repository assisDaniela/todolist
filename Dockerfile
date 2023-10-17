FROM ubuntu:latest AS build
RUN apt-get update

FROM openjdk:21-jdk-slim

COPY . .

RUN apt-get -y install maven
RUN mvn clean install

EXPOSE 8080

COPY --from=build /target/todolist-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]