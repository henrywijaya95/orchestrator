FROM openjdk:11
EXPOSE 8081
ADD target/orchestrator-docker.jar orchestrator-docker.jar
ENTRYPOINT ["java", "-jar", "/orchestrator-docker.jar"]