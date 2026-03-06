# ============================================================
# Multi-stage Dockerfile for QuickExpense Tracker
# Stage 1: Build WAR with Maven
# Stage 2: Deploy to Tomcat 10.1
# ============================================================

# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Runtime Stage ---
FROM tomcat:10.1-jdk21-temurin

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR as ROOT.war (deploys at /)
COPY --from=build /app/target/PersonalExpenseTracker.war /usr/local/tomcat/webapps/ROOT.war

# Expose port (Render uses PORT env var)
EXPOSE 8080

# Start Tomcat with dynamic port support for Render
CMD sed -i "s/8080/${PORT:-8080}/g" /usr/local/tomcat/conf/server.xml && catalina.sh run
