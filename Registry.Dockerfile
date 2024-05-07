FROM openjdk:17-jdk-alpine
COPY app/build/libs/app.jar
ENTRYPOINT ["java", "-cp", "./app/build/libs/app.jar", "cs455.overlay.node.MessagingNode", "$hostname", ]
