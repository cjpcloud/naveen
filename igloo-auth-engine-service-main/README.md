
# igloo-auth-engine-service

Auth Engine  service is accepting ISOMessage as input and perform  validations like card validation, member validation , BAS validation, fraud validation and  invoke ledger service

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview

The `igloo-auth-engine-service` is a Spring Boot application designed to establish gRPC service called "processAuthorizationRequest" .This service used to validate card details , validate member details ,validate member account details , validate BAS and ledger service.

## Features

- Validate Card Details by making gRPC call to Card Service
- Validate Transaction authenticity  by making gRPC call to Fraud Analyzer Service
- Validate Member Details by making gRPC call to Member Service
- Validate Account Details by making gRPC call to Account Service
- Make Basket Analyzing Service  by making gRPC call 
- Post transaction into Ledger by making  gPRC call to Ledger Service

## Installation

### Prerequisites

- Java 21 or higher
- Maven
- gRPC

### Steps

1. Clone the repository:
    ```sh
    git https://github.com/pwc-us-adv-projectigloo/igloo-auth-engine-service.git
    cd igloo-auth-engine-service
    ```

2. Build the project:
    ```sh
    mvn clean install
    ```

3. Run the application:
    ```sh
    mvn spring-boot:run
    ```

## Configuration

The application can be configured using the `application.yml` file located in the `src/main/resources` directory. Below is an example configuration:

```yaml
server:
   port: 8081

grpc:
   server:
      port: 9091
      inProcessName: authengine

service:
   address:
      card: card:9092
      txnFraud: igloo-txn-fraud-analyzer:9095
      member : member:9093
      memberAccount : account:9094
      BAS : bas:9095
      ledger : ledger:9096
```

## Testing

Grpc server Host = grpc://localhost:9091  
Endpoints: `/authengine`

GRPC Request Message:

## Canonical ISO Model Sample
{
{
   isoFormat {
   isoFormatId: "ISO8583"
}
cardDetail {
   panHash: "99db62c28f27bb9e9f02c331f1369a9e"
}
merchantInformation {
   categoryCode: "5912"
}
transactionData {
   ID: "dc86d9d1-1439-44bd-8066-461305e9bc0f"
   amt: "000000000100"
   cur: "USD"
   location: "US"
   localTime {
   seconds: 113456
}
utcTime {
   seconds: 110722180
}
}
messageType {
messageType: "AUTHORIZATION"
}
channelDetails {
channel: "MASTERCARD"
}
}



## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.

## License

This project is licensed under the Nations Benefits's Igloo License.
```
