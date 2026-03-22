# ==========================================
# STAGE 1: Build the application (Builder)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Create the Runtime Image
# ==========================================
# Because you are using WildFly 39 Preview locally for Jakarta EE 11 features,
# we shouldn't use the standard Docker image (which might be an older stable version).
# Instead, we will start with a raw JDK 21 image and install WildFly 39 Preview manually!
FROM eclipse-temurin:21-jre

LABEL maintainer="Your Name"

# 1. Install necessary tools (curl, unzip) and create a user for WildFly
# Note: Ubuntu-based images often already have a user 'ubuntu' with GID/UID 1000.
# We will use GID/UID 1001 for the jboss user to avoid the "GID 1000 already exists" error.
RUN apt-get update && apt-get install -y curl unzip && \
    groupadd -r jboss -g 1001 && useradd -u 1001 -r -g jboss -m -d /opt/jboss -s /sbin/nologin -c "JBoss user" jboss

# 2. Download and extract WildFly Preview 39.0.0.Beta1
# This ensures the Docker container matches your local "dev app" environment EXACTLY.
WORKDIR /opt/jboss
RUN curl -L https://github.com/wildfly/wildfly/releases/download/39.0.0.Beta1/wildfly-preview-39.0.0.Beta1.zip -o wildfly.zip && \
    unzip wildfly.zip && \
    mv wildfly-preview-39.0.0.Beta1 wildfly && \
    rm wildfly.zip && \
    chown -R jboss:jboss /opt/jboss/wildfly && \
    chmod -R ug+rwX /opt/jboss/wildfly

# Switch to the jboss user
USER jboss
WORKDIR /opt/jboss/wildfly

# 3. Download the PostgreSQL JDBC Driver
RUN mkdir -p /opt/jboss/wildfly/custom-libs && \
    curl -L https://jdbc.postgresql.org/download/postgresql-42.7.8.jar -o /opt/jboss/wildfly/custom-libs/postgresql-42.7.8.jar

# 4. Copy the CLI configuration script
COPY setup-wildfly.cli /opt/jboss/wildfly/custom-libs/

# 5. Run the CLI script to configure the server (adds driver, datasource, and JMS queue)
# We set the environment variables with dummy values just so the script compiles,
# the real values will be injected by docker-compose at runtime!
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/custom-libs/setup-wildfly.cli

# 6. Copy your compiled application into the deployments folder
COPY --from=builder /app/target/jakarta-ee11-library-manager-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

EXPOSE 8080
EXPOSE 9990

# Start WildFly in standalone-full mode so JMS works, and bind to 0.0.0.0
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml"]
