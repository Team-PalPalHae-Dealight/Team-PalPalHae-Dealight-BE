FROM openjdk:17-jdk
LABEL maintainer="Dealight-BE"

ARG JAR_FILE=build/libs/dealight-be-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} dealight-be.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/dealight-be.jar"]
