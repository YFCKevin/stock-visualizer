FROM openjdk:17-jdk-alpine

VOLUME /tmp

WORKDIR /

ARG JAR_FILE=target/stockMate-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=Asia/Taipei", "-jar", "/app.jar"]
