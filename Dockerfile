# Use official Tomcat base image with JDK 17
FROM tomcat:10.1-jdk17

# Remove default webapps to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR file to Tomcat's webapps directory
COPY /target/Stripe-Project-Sample-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Optionally set environment variables with defaults (for local testing only)
# These will be overridden in production (like Render)
ENV STRIPE_SECRET_KEY=dummy_key \
    STRIPE_WEBHOOK_SECRET=dummy_webhook

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
