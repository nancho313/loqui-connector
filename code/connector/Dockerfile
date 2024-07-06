FROM openjdk:21-slim
LABEL org.opencontainers.image.source="https://github.com/nancho313/loqui-connector"
LABEL org.opencontainers.image.description="Java Microservice that handles the connections made by the users."
COPY boot/target/connector*.jar app.jar
CMD ["java", "-jar", "/app.jar"]