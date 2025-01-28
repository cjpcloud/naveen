# igloo-iso8583-adapter-service

## Overview
This microservice is responsible for receiving ISO8583 messages and processing them based on their Message Type Indicator (MTI).It serves as a demo for ISO8583 adapter service, which is a TCP/IP socket listener. Note that this service is intended for demonstration purposes and might be removed once real implementation and testing are possible.

## Workflow

 ```mermaid  
flowchart TD; 
    A[REST to Receive ISO8583 message] --> B(Parse ISO8583 message and perform message structure validation);  
    B --> C{Is ISO8583 message a valid auth request?}; 
    C -->|YES| D[Perform Hash on PAN];
    C -->|NO| E[Consider failure and send to create ISO8583 auth response];  
    D --> F[Build Canonical model to pass to auth engine];  
    F --> G[Call Auth engine gRPC to pass Canonical model request];  
    E --> H[Build ISO8583 auth response];
    H --> I[Send response back];
 ```  

##   

## Table of Contents
1. [Getting Started](#getting-started)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Running the Service](#running-the-service)
6. [API Endpoints](#api-endpoints)
7. [Testing](#testing)
8. [Logging and Monitoring](#logging-and-monitoring)
9. [Troubleshooting](#troubleshooting)
10. [Contributing](#contributing)
11. [License](#license)

## Getting Started
These instructions will help you set up and run the microservice on your local machine for development and testing purposes.

## Prerequisites
- Java 21 or higher
- Maven

## Installation
1. Clone and build `igloo-base-bom`, `igloo-service-bom` and  `igloo-protobuf-common` repositories.

2. Clone the repository:
    ```sh  
    git clone git@github.com:pwc-us-adv-projectigloo/igloo-iso8583-adapter-service.git
    cd igloo-iso8583-adapter-service
    ```  
3. Install dependencies:
    ```sh  
    mvn clean install  
    ```

## Configuration
1. The application can be configured using the `application.yml` file located in the `src/main/resources` directory. Below is an example configuration:
```yaml

spring:
  main:
    allow-circular-references: true
    allow-bean-definition-overriding: true
  profiles:
    active:
      - handler

server:
  port: 8080

config:
  secrets:
    enabled: true

authengine:
  service:
    port: authengine:9091

grpc:
  server:
    port: 9090

logging:
  level:
    io.grpc: TRACE

springdoc:
  api-docs:
    path: /igloo/api-docs
  swagger-ui:
    path: /igloo/
```

## Running the Service
1. Start the microservice:
    ```sh  
    mvn spring-boot:run  
    ```  

## API Endpoints

### Swagger UI
You can explore the API endpoints using Swagger UI. Once the application is running, navigate to:  
http://localhost:8080/igloo/swagger-ui/index.html		

## Testing
1. Run unit tests:
    ```sh  
    mvn test  
    ```  

## Logging and Monitoring
- Logs are configured to output to the console and can be found in the logs directory.
- Monitoring can be set up using Prometheus metrics and health checks.

## Troubleshooting
- **Common issues and solutions**:
    - Missing dependencies: Run `mvn clean install` to ensure all dependencies are properly installed.
- **How to debug problems**:
    - Check the logs for any error messages.
    - Use IDE debug tools to step through the code.

## Contributing
1. Clone the repository.
2. Create a new feature branch:
    ```sh  
    git checkout -b feature/your-feature-name  
    ```  
3. Commit your changes:
    ```sh  
    git commit -m "Add some feature"  
    ```  
4. Push to the branch:
    ```sh  
    git push origin feature/your-feature-name  
    ```  
5. Create a new Pull Request.

## License
This project is licensed under <> License - see the [LICENSE](LICENSE) file for details.  