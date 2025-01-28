# Stage 1: Build the JAR file
FROM eclipse-temurin:17-jdk-alpine AS builder

ARG USERNAME=service-account
ARG USER_UID=1002
ARG USER_GID=$USER_UID

# Create non-root user
RUN addgroup -g $USER_GID $USERNAME \
    && adduser -u $USER_UID -G $USERNAME -h /home/$USERNAME -D $USERNAME

USER $USERNAME
WORKDIR /app

# Copy the project and build it
COPY --chown=service-account:service-account ./target/*.jar app.jar
RUN ls -latr

# Stage 2: Create minimal runtime environment using scratch
FROM scratch

# Copy the required files from the builder stage
COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/group /etc/group
COPY --from=builder /app/app.jar /app/app.jar

WORKDIR /app

# Switch to non-root user
USER service-account

# Command to run the application
CMD ["java", "-jar", "app.jar"]
