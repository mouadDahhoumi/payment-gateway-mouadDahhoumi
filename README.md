# payment-gateway-mouadDahhoumi
Processout Take-home coding challenge 



Processout Take-home coding challenge

# How to run

## Build scripts

This app is bundled with a build and run script to build the API and launch the API with a RabbitMQ Docker container.
To run, use:
```powershell
chmod +x scripts/build-and-run.sh

./scripts/build-and-run.sh
```

To build and run manually:
```powershell
// Build and generate the jar
./mvnw clean package

// Build Docker images
docker-compose build

// Start containers
docker-compose up
```

- The API can be accessed at http://localhost:8080
- The Swagger page can be accessed at http://localhost:8080/swagger-ui/index.html#/. It provides a description of the API using the OpenAPI Specification. It also provides a web-based interface where one can test API endpoints

# Project structure

This project uses a layered architecture: It is a design approach where the various components and responsibilities of an application are organized into distinct layers. Each layer has a specific role and interacts with the layers above and below it. This architectural style helps to separate concerns, improve maintainability, and promote scalability in the application. The main layers in this application are:
1. **Presentation Layer (Controller/Endpoint):** This layer handles HTTP requests, user interactions, and input validation. It typically consists of endpoints that receive incoming requests, process them, and return appropriate responses. Spring Boot's @RestController` annotation is used in this layer.

2. **Service Layer:** The service layer contains the business logic of the application. It acts as an intermediary between the presentation layer and the data access layer. Services encapsulate the application's functionality, perform data processing, and contain business rules. These classes are annotated with `@Service`.

3. **Data Access Layer (Repository/DAO):** This layer is responsible for interacting with the database or any external data sources. Spring Boot provides `@Repository` and Spring Data JPA for simplifying data access.

4. **Model Layer:** This layer represents the core domain objects or entities of the application. These objects encapsulate the data and business logic specific to your application's domain. In Spring Boot, these classes are annotated with `@Entity` when using JPA.

5. **Configuration Layer:** Spring Boot applications often use configuration classes to set up beans, manage external configurations, and configure various aspects of the application. These classes are typically annotated with `@Configuration`.

6. **Utility or Helper Classes:** These classes provide utility functions or helper methods used across different layers of the application.

Moreover, this project also has a special validation layer that is mainly used by the controllers.

# Functional Requirements:

-  A merchant can process transactions coming from a customer and submit each payment to a bank (A mock bank in our case)
-  A merchant can retrieve the details of all his transactions at any time

# Non-Functional requirements
To build a high-performance and reliable payment processing system that can handle a large volume of transactions while ensuring responsiveness and fault tolerance, an event-driven architecture for asynchronous communication with banks is chosen. In this context, The PaymentGateway is the producer and PaymentProcessor is the consumer. The PaymentProcessor is the bridge between the Payment gateway and the bank. In summary, using a RabbitMQ queue in the payment processing system helps improve performance by enabling asynchronous processing, load balancing, fault tolerance, scalability, decoupling of components, buffering, and monitoring.

# High Level Design

![highLvlDesign](https://github.com/mouadDahhoumi/payment-gateway-mouadDahhoumi/assets/82587821/5ccebaa3-38b7-4f27-95d1-b11cff0ab97c)

1. To process a payment, the merchant is invited to register within the application. It is done through the api/merchants Post endpoint by providing a name. Once this is done. A merchant should be able to process a transaction by submitting a SubmitPaymentRequestBody that contains all the necessary information concerning a Transaction (amount, currency, card details). For now, the merchant should also provide its merchantId in that request body (JWT Auth should be introduced, see areas for improvements). The card details are validated by using the built in jakarta.validation and also by using a custom constraint that takes advantage of Luhn algorithm.

2. Now that the card details are validated, the transaction is saved to the database with a “PENDING" status.

3. After saving the transaction, the transaction data is sent to a RabbitMQ queue for further processing. This step indicates that the payment transaction will be processed asynchronously. (The endpoint immediately returns the id of the created transaction and the status as “PENDING” and doesn't wait for the bank response.) This can be seen in the sequence diagram.

4. PaymentProcessor is responsible for processing payment transactions, interacting with a bank service, and handling retries in case of network failures or declined transactions. The PaymentProcessor consumes sequentially each submitted Transaction object, and sends it over to the mock bank and wait for a response. It processes payment transactions in a loop with retry logic. It checks the BankResponse to determine whether the transaction was accepted, declined due to a network failure, or declined for some other reason and decide if it should proceed with the retry logic .

5. MockBank is an Implementation of the IBank interface, which simulates the behavior of a bank for testing purposes within a payment gateway system. It intentionally introduces delays and random outcomes to simulate the behavior of a real bank for testing purposes. This class is used within the payment gateway system to mimic the interaction with a bank during transaction processing, allowing to test the payment gateway's functionality without connecting to a real banking system. It can easily be switched for a real implementation. The mock bank delivers the status of the transaction, a date and a decline reason if the payment is declined. In case of a network failure, The operation is retried 3 times.

6. PaymentProcessor update the transaction after getting a final response from the bank.

![sequenceDiag](https://github.com/mouadDahhoumi/payment-gateway-mouadDahhoumi/assets/82587821/ae2d77ec-9710-4965-85b3-e1ef351f7d91)

# Payment Gateway API Documentation

This section documents the endpoints to process transactions and retrieve their details.

## Register Merchant

Process a transaction by providing payment details.

- **URL**: `/api/merchants`
- **Method**: `POST`

### Request

Example Request Body:
```json
{
    "name": hind
}
```
### Response

- **Status Codes**:
    - `201 Created` - The transaction is saved to the db and the payment processor will process it.
    - `400 Bad Request` - Invalid request format.
### Successful Response (201 OK)

**Response Header**:
 `location: /api/merchants/4`


## Process Transaction
Process a transaction by providing payment details.

- **URL**: `/api/transactions`
- **Method**: `POST`

### Request

Example Request Body:
```json
{
    "merchantId": 1,
    "amount": 100,
    "expiryMonth": 2,
    "expiryYear": 2202,
    "owner": "hind",
    "ccv": "222",
    "cardNumber": "4916 5027 3215 3333",
    "currency": "USD"
}
```
### Response

- **Status Codes**:
    - `200 OK` - The transaction is saved to the db and the payment processor will process it.
    - `401 Unauthorized`  - merchnat with merchantId is not found.
    - `400 Bad Request` - Invalid request format.
    - `500 Internal Server Error` - Unexpected server error

### Successful Response (200 OK)
```json
{
    "transactionId": 8,
    "status": "PENDING"
}
```

### Success response payload

| Field         | Description                       | Values                      |
|---------------|-----------------------------------|-----------------------------|
| transactionId | transaction identifier            | long                        |
| status        | current status of the transaction | Approved, Declined, Pending |

## Retrieve details of a previously made transaction by providing the transaction ID and the merchantId in a request body.

- **URL**: `/api/transactions/{transactionId}`
- **Method**: `GET`

### Request

- **Parameters:**
    - `transactionId` (string: required) - The unique identifier of the transaction to retrieve.
    
- **Request Body**:
```json
{
    "merchantId": 1
}
```

### Response

- **Status Codes**:
    - `200 OK` - Successful retrieval. Returns transaction details.
    - `404 Not Found` - Transaction with the provided IDs does not exist.
    - `400 Bad Request` - Invalid request format.

Successful Response (200 OK)
```json
{
    "merchantId": 1,
    "transactionId": 1,
    "amount": 100.0,
    "submissionDate": "2023-09-18T12:01:41.349+00:00",
    "transactionDate": null,
    "status": "PENDING",
    "declineReason": null,
    "currency": "USD",
    "cardDetails": {
        "expiryMonth": 2,
        "expiryYear": 2202,
        "owner": "hind",
        "cardNumber": "***************3333",
        "ccv": "222"
    }
}
```

Success response payload

| Field           | Description                                                    | Values                      |
|-----------------|----------------------------------------------------------------|-----------------------------|
| transactionId   | Transaction id                                                 | long                        |
| merchantId      | Merchant Id                                                    | long                        |
| amount          | Payment amount                                                 | double                      |
| submissionDate  | The date the merchant submitted the transaction to the gateway | date                        |
| transactionDate | The date of when the transaction processing ended              | date                        |
| currency        | 3 letter currency code                                         | USD,EUR,GBP,JPY,CAD,AUD     |
| status          | Current status of the transaction                              | Approved, Declined, Pending |
| declineReason   | Decline reason if status is Declined                           | string                      |
| cardNumber      | maskedCardNumber                                               | string                      |
| expiryMonth     | Expiry month of the card                                       | int                         |
| expiryyear      | Expiry year of the card                                        | int                         |
| ccv             | Three or four digits nuber                                     | string                      |

## Retrieve All Transactions Details
Retrieve details of a all transactions by providing the merchantId in the request body.

- **URL**: `/api/transactions`
- **Method***: `GET`

### Request
- **Request Body**:
    - JSON object representing the merchantId.

### Example Request Body:
```json

{
    "merchantId": 1
}
```

### Response

- **Status Codes**:
    - `200 OK` - Successful retrieval. Returns all transactions details.
    - `404 Not Found` - No transaction was found for the provided merchantId.
    - `400 Bad Request` - Invalid request format.

### Successful Response (200 OK)

```json
[
    {
        "transactionId": 1,
        "amount": 100.0,
        "submissionDate": "2023-09-18T12:01:41.349+00:00",
        "transactionDate": null,
        "status": "PENDING",
        "declineReason": null,
        "currency": "USD",
        "cardDetails": {
            "expiryMonth": 2,
            "expiryYear": 2202,
            "owner": "hind",
            "cardNumber": "***************3333",
            "ccv": "222"
        }
    }
]
```

## Retrieve All Pending/Declined/Approved Transactions Details

Retrieve details of a all transactions by providing the merchantId in the request body.

- **URL**: `/api/transactions/pending|declined|approved`
- **Method**: `GET`
These are three different endpoints, each one provide all the transactions with the desired status

### Request

- **Request Body**: JSON object representing the merchantId.

### Example Request Body:
```json
{
    "merchantId": 1
}
```

## Calculate revenue
Calculate the revenue by suming the amounts of all accepted transactions.

- **URL**: `/api/transactions/revenue`
- **Method**: `GET`
### Request

Body: JSON object representing the merchantId.

### Example Request Body:
```json
{
    "merchantId": 1
}
```
## Calculate Bank Approval Rate
Calculate the bank approval rate by assessing the ratio of accepetd transaction to the total number of transactions.

- **URL**: `/api/transactions/revenue`
- **Method**: `GET`

### Request

- **Request Body**: JSON object representing the merchantId.

### Example Request Body:
```json
{
    "merchantId": 1
}
```

## Assumptions
- It's assumed that the Bank can provide a response within a reasonable timeframe, typically in a matter of seconds. This is especially important for online purchases and point-of-sale transactions. This why the bank in this app is mocked so that can it can provide a response from 1 to 10 seconds.
- The BankService should have mechanisms in place to handle timeouts effectively. For example, if a response is not received within a specified time, the service may retry the request or return an appropriate error code. In the context of this application, the bank doesn't handles retries and only send a responde indicating a network failure.
- In this implementation, It was assumed that the bank can provide these decline reasons:

| Decline Code                   | Decline Text                  |
|--------------------------------|-------------------------------|
| CANCELED_CARD                  | Canceled card                 |
| EXPIRED_CARD                   | Expired card                  |
| LACK_OF_FUND                   | Lack of fund                  |
| INCORRECT_PAYMENT_INFORMATION  | Incorrect payment information |
| UNVERIFIED_CUSTOMER            | Unverified customer           |
| NETWORK_FAILURE                | Network failures or timeouts  |

- The bank randomly pick if the transaction is Accepted or Declined. If declined, one of these decline reasons is chosen. We can add more of the same reason in the DECLINE_REASONS array so that the reason has a higher probability for being selected (for testing purposes). The same logic can be applied to wether a transaction is accepted or refused
- The PaymentProcess will retry the processing of transaction if it faces a Network Failure code from the bank. It is considered that Network Failure is a soft decline so it is okay to retry. Theses policies can easily be defined in the application 
- It is assumed that the bank cannot intercat with the app database. The PaymentProcessor is the bridge beetween the app and the bank
- Assuming the app will intercat with different banks, an interface IBank is provided. It is a contract that should allow multiple banks implementation
- The time it takes to process a payment can vary significantly based on several factors, so an event driven architecture is suitable as it reduces the need for synchronous blocking calls and improve system responsiveness.


## Areas for improvement

- Introduce a front end admin page that provides a UI to process payments and dashboards,
- Introduce JWT for Authentication and especially Authorization since we are now required to provide the merchanId in every request body
- Set up Continuous Integration (CI) for this Spring project on GitHub, (we can use GitHub Actions)
- Implement robust error handling mechanisms for the event driven architecture
- For now, tha app uses an in memory database (H2) that mimics a traditional sql db. It would be better if we host a proper sql db in a container.
- Separating the consumer app and run it on its own container and environment should give more flexibilty and scalability. 
- Increase test coverage

## Cloud technologies

The choice of cloud services depends on budget, and familiarity with cloud platforms. Here are some popular cloud technologies that might be considered for hosting this app

**Amazon Web Services (AWS)**:
  -  Amazon EC2: You can use Amazon Elastic Compute Cloud (EC2) to deploy virtual servers where you can run your Spring Boot application and RabbitMQ.
  -  Amazon MQ AWS offers a managed message broker service called Amazon MQ, which supports RabbitMQ. It simplifies the setup and management of RabbitMQ clusters.
  -  Amazon RDS (Relational Database Service): RDS offers managed database services for various relational databases such as MySQL, PostgreSQL, SQL Server, and Oracle. It provides features like automated backups, high availability, and scalability.
    
**Kubernetes (K8s)**: Kubernetes is a container orchestration platform that can run Spring Boot applications and RabbitMQ in containers. It can be deployed to AWS.

## Bonus points
- Using an event-driven architecture with rabbitmq to process transacction events asynchronously, which can significantly improve system responsiveness and throughput and guarantee a real-time processing.
- Implemented the Luhn algorithm for card number validation.
- Provided a Dockerfile for the Spring app, Docker Compose file to run a multi container app and scripts to build and run automatically.
- Implemented a retry policy in the Payment Processor in order to retry the process if confronted to network issues.
- Provided some additional endpoints to gain insights about the transactions such as the bank approval rate, which is a crucial performance metric for payment processing.
- Externalized many values in the configuration file (application.properties) such as the name and the credentials of the queue, max retries allowed by the PaymentProcessor etc. This is crucial when considering different environments
- Logging using Log4j 
