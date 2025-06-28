# Use official Tomcat base image with JDK 17
FROM tomcat:10.1-jdk17

# Remove default webapps (like ROOT)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from local build context to webapps directory
COPY target/Stripe-Project-Sample-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war



# Expose default Tomcat port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]
