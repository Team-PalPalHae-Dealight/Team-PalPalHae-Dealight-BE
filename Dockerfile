FROM openjdk:17-jdk
LABEL maintainer="Dealight-BE-Team"

WORKDIR /app
COPY dealight-be-0.0.1-SNAPSHOT.jar dealight-be.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","dealight-be.jar"]
