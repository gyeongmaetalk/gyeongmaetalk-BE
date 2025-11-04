FROM eclipse-temurin:17-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} auctiontalk.jar
ENTRYPOINT ["java","-jar","/auctiontalk.jar"]