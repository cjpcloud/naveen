FROM eclipse-temurin:21-jre

ARG USERNAME=service-account
ARG USER_UID=1002
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME

USER $USERNAME

WORKDIR /app


COPY --chown=service-account:service-account ./target/*.jar app.jar

RUN ls -latr

RUN chmod 777 app.jar

RUN ls -latr

# RUN java -jar app.jar

CMD ["java" , "-jar", "app.jar"]