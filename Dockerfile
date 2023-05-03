FROM maven:3.9.1 AS build
RUN mkdir /app
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

FROM openjdk:17 AS image-jira
LABEL authors="somebody"
RUN mkdir /app
COPY --from=build app/target/jira-1.0.jar /jira-1.0.jar
WORKDIR /app
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/jira-1.0.jar"]
