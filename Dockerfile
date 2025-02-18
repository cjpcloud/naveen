# First stage: Build JRE and package the app
FROM eclipse-temurin:17-jdk-alpine AS jre-builder

WORKDIR /opt/app

# Install required tools
RUN apk update && apk add --no-cache tar binutils wget

# Install Maven manually
ENV MAVEN_VERSION 3.5.4
RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    mv apache-maven-$MAVEN_VERSION /usr/lib/mvn
ENV PATH="/usr/lib/mvn/bin:${PATH}"

# Copy source code and package the application
COPY . /opt/app
RUN mvn clean package -DskipTests && ls -l target/

# Extract dependencies for jlink
RUN jdeps --ignore-missing-deps -q --recursive --multi-release 17 --print-module-deps \
    --class-path 'BOOT-INF/lib/*' target/CRUD-SampleApp-Java.jar > modules.txt

# Build minimal JRE
RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules $(cat modules.txt) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /optimized-jdk-17

# Second stage: Use custom JRE and run the app
FROM alpine:latest
ENV JAVA_HOME=/opt/jdk/jdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy the minimal JRE
COPY --from=jre-builder /optimized-jdk-17 $JAVA_HOME

# Create app user
ARG APPLICATION_USER=spring
RUN addgroup --system $APPLICATION_USER && adduser --system $APPLICATION_USER --ingroup $APPLICATION_USER

# Create application directory
RUN mkdir /app && chown -R $APPLICATION_USER /app

# Copy the built JAR file correctly
COPY --from=jre-builder /opt/app/target/CRUD-SampleApp-Java.jar /app/CRUD-SampleApp-Java.jar

WORKDIR /app
USER $APPLICATION_USER

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/CRUD-SampleApp-Java.jar" ]
