# Use official Tomcat base image with JDK 17
FROM tomcat:10.1-jdk17

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR file to Tomcat
COPY target/Stripe-Project-Sample-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose default Tomcat port
EXPOSE 8080

# Run Tomcat
CMD ["catalina.sh", "run"]
