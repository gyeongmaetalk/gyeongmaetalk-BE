FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} auctiontalk.jar
ENTRYPOINT ["java","-jar","/auctiontalk.jar"]