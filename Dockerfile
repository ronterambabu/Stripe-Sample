# Use official JDK 17 base image
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/Stripe-Project-Sample-0.0.1-SNAPSHOT.jar app.jar

# Optionally set environment variables (you can override them at runtime)
ENV STRIPE_SECRET_KEY=dummy_key \
    STRIPE_WEBHOOK_SECRET=dummy_webhook

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
