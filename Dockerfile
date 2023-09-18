FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/payment-gateway.jar payment-gateway.jar
ENTRYPOINT ["java", "-jar","payment-gateway.jar"]
EXPOSE 8080