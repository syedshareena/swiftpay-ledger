SwiftPay — Ledger Service (Service B)
Overview
The Ledger Service is the core payment processor of the SwiftPay platform. It consumes PaymentInitiated events from Kafka, performs atomic debit/credit operations on user accounts, updates transaction status, and exposes a transaction history API for financial reporting.

Architecture
Kafka Topic: payment-initiated
│
▼
PaymentConsumer (Kafka Listener)
│
├──▶ Validate sender balance
│
├──▶ Atomic DB Transaction
│       ├── Debit sender account
│       └── Credit receiver account
│
├──▶ Update Transaction status (COMPLETED / FAILED)
│
└──▶ Kafka Topic: payment-completed / payment-failed

Tech Stack
Technology	Version	Purpose
Java	21	Core language
Spring Boot	3.5.14	Application framework
PostgreSQL	15	Account & transaction storage
Apache Kafka	3.7	Event streaming
Redis	7.0	Caching
Swagger/OpenAPI	3.0	API documentation
Docker	Latest	Containerization
GitHub Actions	-	CI/CD pipeline

Features
Kafka Consumer — Listens to 'payment-initiated' topic
Atomic Operations — Debit/Credit within @Transactional boundary
Retry Mechanism — Handles temporary DB failures
Transaction History — REST API for user transaction reporting
API Docs — Fully documented with Swagger/OpenAPI
Health Check — Spring Actuator health endpoint
Containerized — Docker-ready
CI/CD — GitHub Actions pipeline

API Endpoints
Method	Endpoint	Description	Status Codes
GET	/v1/ledger/transactions/{userId}	Get transaction history	200, 404
GET	/actuator/health	Service health check	200

Request & Response
Get Transaction History
Request:
```bash
GET /v1/ledger/transactions/3fa85f64-5717-4562-b3fc-2c963f66afa6
```
Success Response (200 OK):
```json
[
{
"id": "abc12345-1234-1234-1234-abcdef123456",
"senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
"receiverId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
"amount": 500.00,
"currency": "INR",
"status": "COMPLETED",
"createdAt": "2026-05-03T10:00:00"
}
```
]

Kafka Events
Event	Topic	Direction	Description
PaymentInitiated	payment-initiated	Consumed	Triggers payment processing
PaymentCompleted	payment-completed	Produced	Emitted on success
PaymentFailed	payment-failed	Produced	Emitted on failure

Getting Started
Prerequisites
Java 21
Maven 3.8+
Docker Desktop
Service A (Transaction Gateway) must be running
1. Clone the repository
```bash
git clone https://github.com/syedshareena/swiftpay-ledger.git
cd swiftpay-ledger
```
2. Start infrastructure (PostgreSQL + Kafka + Redis)
```bash
docker-compose up -d
```
3. Run the service
```bash
mvn spring-boot:run
```
4. Access Swagger UI

http://localhost:8081/swagger-ui.html

5. Check Health

http://localhost:8081/actuator/health

Docker
Build Docker image
```bash
mvn clean package -DskipTests
docker build -t swiftpay-ledger .
```
Run with Docker
```bash
docker run -p 8081:8081 swiftpay-ledger
```

Project Structure

ledger-service/
├── src/
│   └── main/
│       ├── java/com/swiftpay/ledger_service/
│       │   ├── config/          # Kafka configuration
│       │   │   └── KafkaConfig.java
│       │   ├── consumer/        # Kafka event consumers
│       │   │   └── PaymentConsumer.java
│       │   ├── controller/      # REST API controllers
│       │   │   └── LedgerController.java
│       │   ├── dto/             # Data Transfer Objects
│       │   │   └── PaymentEvent.java
│       │   ├── model/           # JPA Entities
│       │   │   ├── Account.java
│       │   │   └── Transaction.java
│       │   ├── repository/      # Spring Data JPA repositories
│       │   │   ├── AccountRepository.java
│       │   │   └── TransactionRepository.java
│       │   └── service/         # Business logic
│       │       └── LedgerService.java
│       └── resources/
│           └── application.properties
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI/CD
├── Dockerfile
└── pom.xml


CI/CD Pipeline
GitHub Actions workflow automatically:
Compiles Java code
Runs unit & integration tests
Builds Docker image

Configuration
Property	Value
Server Port	8081
Database	PostgreSQL (swiftpay)
Kafka Consumer Group	ledger-group
Kafka Input Topic	payment-initiated
Kafka Output Topics	payment-completed, payment-failed

Testing & Verification

Health Check
http://localhost:8081/actuator/health

Swagger UI
http://localhost:8081/swagger-ui.html

Payment Completed
```json

{

"transactionId": "txn-screen-003",

"senderId": "550e8400-e29b-41d4-a716-446655440000",

"receiverId": "550e8400-e29b-41d4-a716-446655440001",

"amount": 50.00,

"currency": "USD"

}

```
Expected: COMPLETED

Insufficient Funds
```json

{

"transactionId": "txn-screen-004",

"senderId": "550e8400-e29b-41d4-a716-446655440000",

"receiverId": "550e8400-e29b-41d4-a716-446655440001",

"amount": 999999.00,

"currency": "USD"

}

```
Expected: FAILED

Transaction History
userId: 550e8400-e29b-41d4-a716-446655440000

Database Verification
docker exec -it swiftpay-postgres psql -U postgres -d swiftpay -c "SELECT id, status, amount FROM transactions;"

## Load Testing

Service B (Ledger Service) was tested indirectly as part of the full system load test.

- Trigger Source: Kafka events from Transaction Gateway
- Throughput: ~250 TPS
- Total Events Processed: ~1,000,000
- Consumer Group: `ledger-group`

### Purpose

- Validate Kafka consumer performance under load
- Ensure atomic DB transactions (debit/credit)
- Verify correct event processing (COMPLETED / FAILED)

## PCAP Capture

A single PCAP capture was taken during the full system load test to analyze inter-service communication.

### Tool Used
- Wireshark

### Capture Details
- Interface: Loopback (localhost)
- File: `load_test_capture.pcapng`

### Observed Traffic
- Kafka consumption from `payment-initiated` topic
- Consumer group activity (`ledger-group`)
- Kafka responses and heartbeats
- Event publishing to:
  - `payment-completed`
  - `payment-failed`

### Note

Since the Ledger Service operates as a Kafka consumer, its activity is captured as part of the shared system-level network traffic rather than isolated API calls.

The PCAP demonstrates:
- Event-driven communication between services
- Real-time processing under load
- Distributed system interaction via Kafka

Author
Shareena Syed  
Java Full Stack Developer  
Java 21 | Spring Boot | Kafka | Redis | Docker
