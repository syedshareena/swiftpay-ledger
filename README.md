# SwiftPay — Ledger Service (Service B)

## Overview

The **Ledger Service** is the core payment processor of the SwiftPay platform. It consumes **PaymentInitiated** events from Kafka, performs atomic debit/credit operations on user accounts, updates transaction status, and exposes a transaction history API for financial reporting.

---

## Service Responsibilities

- Consume PaymentInitiated events from Kafka
- Validate sender account balance
- Perform atomic debit and credit operations
- Update transaction status (COMPLETED / FAILED)
- Publish PaymentCompleted and PaymentFailed events
- Expose transaction history APIs
- Support event-driven payment processing

---

## Architecture


              Kafka Topic
        payment-initiated
                  │
                  ▼
        PaymentConsumer
                  │
        Validate Sender Balance
                  │
        @Transactional
      ┌───────────┴───────────┐
      │                       │
 Debit Sender          Credit Receiver
      │                       │
      └───────────┬───────────┘
                  │
     Update Transaction Status
                  │
        ┌─────────┴─────────┐
        │                   │
payment-completed   payment-failed
```

## Payment Processing Flow

1. Consume PaymentInitiated event from Kafka.
2. Validate sender account balance.
3. Start database transaction.
4. Debit sender account.
5. Credit receiver account.
6. Update transaction status.
7. Publish PaymentCompleted or PaymentFailed event.

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.5.14 | Application framework |
| PostgreSQL | 15 | Account & Transaction storage |
| Apache Kafka | 3.7 | Event streaming |
| Redis | 7.0 | Caching |
| Swagger/OpenAPI | 3.0 | API documentation |
| Docker | Latest | Containerization |
| GitHub Actions | - | CI/CD Pipeline |

---

## Features

- Kafka Consumer for payment processing
- Atomic Debit/Credit operations using `@Transactional`
- Retry mechanism for temporary failures
- Transaction History REST API
- Swagger/OpenAPI documentation
- Spring Boot Actuator Health endpoint
- Docker support
- GitHub Actions CI/CD Pipeline

---

## Kafka Events

| Event | Topic | Direction | Description |
|--------|-------|-----------|-------------|
| PaymentInitiated | payment-initiated | Consumed | Starts payment processing |
| PaymentCompleted | payment-completed | Produced | Published after successful transaction |
| PaymentFailed | payment-failed | Produced | Published when transaction fails |

---

## API Endpoints

| Method | Endpoint | Description | Status Codes |
|---------|----------|-------------|--------------|
| GET | `/v1/ledger/transactions/{userId}` | Get transaction history | 200, 404 |
| GET | `/actuator/health` | Health Check | 200 |
| GET | `/swagger-ui.html` | Swagger UI | 200 |

---

# Request & Response

## Transaction History Request

```http
GET /v1/ledger/transactions/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

## Success Response (200 OK)

```json
[
  {
    "id": "abc12345-1234-1234-1234-abcdef123456",
    "senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "receiverId": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
    "amount": 500.00,
    "currency": "INR",
    "status": "COMPLETED",
    "createdAt": "2026-05-03T10:00:00"
  }
]
```

---

# Getting Started

## Prerequisites

- Java 21
- Maven 3.8+
- Docker Desktop
- Transaction Gateway Service must be running

---

## Clone Repository

```bash
git clone https://github.com/syedshareena/swiftpay-ledger.git

cd swiftpay-ledger
```

---

## Start Infrastructure

```bash
docker-compose up -d
```

This starts:

- PostgreSQL
- Kafka
- Redis

---

## Run the Service

```bash
mvn spring-boot:run
```

---

## Swagger UI

```
http://localhost:8081/swagger-ui.html
```

---

## Health Check

```
http://localhost:8081/actuator/health
```

---

# Docker

## Build Docker Image

```bash
mvn clean package -DskipTests

docker build -t swiftpay-ledger .
```

---

## Run Docker Container

```bash
docker run -p 8081:8081 swiftpay-ledger
```

---

# Project Structure

```text
ledger-service/
│
├── src/
│   └── main/
│       ├── java/com/swiftpay/ledger_service/
│       │   ├── config/              # Kafka Configuration
│       │   ├── consumer/            # Kafka Consumers
│       │   ├── controller/          # REST Controllers
│       │   ├── dto/                 # DTO Classes
│       │   ├── model/               # JPA Entities
│       │   ├── repository/          # Spring Data JPA Repositories
│       │   └── service/             # Business Logic
│       │
│       └── resources/
│           └── application.properties
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── .github/workflows/ci.yml
```

---

# CI/CD Pipeline

GitHub Actions automatically:

- Compile Java code
- Run Unit & Integration Tests
- Build Docker Image

---

# Configuration

| Property | Value |
|-----------|-------|
| Server Port | 8081 |
| Database | PostgreSQL (swiftpay) |
| Kafka Consumer Group | ledger-group |
| Kafka Input Topic | payment-initiated |
| Kafka Output Topics | payment-completed, payment-failed |

---

# Testing & Verification

### Health Check

```
http://localhost:8081/actuator/health
```

### Swagger UI

```
http://localhost:8081/swagger-ui.html
```

### Test Scenarios

| Scenario | Expected Result |
|----------|-----------------|
| Valid Payment | COMPLETED |
| Insufficient Funds | FAILED |
| Transaction History | Returns user transactions |

---

# Load Testing

The Ledger Service was tested as part of the complete SwiftPay payment flow.

### Test Details

- Trigger Source: Kafka events from Transaction Gateway
- Throughput: **250 TPS**
- Total Events Processed: **~1,000,000**
- Consumer Group: **ledger-group**

### Purpose

- Validate Kafka consumer performance
- Verify atomic debit/credit operations
- Verify transaction status updates
- Validate high-throughput event processing

---

# PCAP Capture

A single PCAP capture was recorded using **Wireshark** during system load testing.

### Capture Details

- Interface: Loopback (localhost)
- File: `load_test_capture.pcapng`

### Observed Traffic

- Kafka consumption from payment-initiated
- Consumer Group activity
- Kafka heartbeats
- payment-completed events
- payment-failed events

---

# Design Decisions

### Why @Transactional?

Ensures debit and credit operations execute atomically. If any operation fails, the complete transaction is rolled back.

### Why Kafka?

Kafka enables asynchronous communication between microservices and improves scalability.

### Why PostgreSQL?

PostgreSQL provides ACID-compliant transactions required for financial systems.

### Why Retry Mechanism?

Retry logic helps recover from temporary failures such as transient database issues.

---

# Related Repository

The payment initiation component is implemented in the **SwiftPay Transaction Gateway**.

Repository:

https://github.com/syedshareena/swiftpay-transaction-gateway

---

# Author

**Shareena Syed**

Java Full Stack Developer

Java 21 | Spring Boot | Apache Kafka | Redis | PostgreSQL | Docker
