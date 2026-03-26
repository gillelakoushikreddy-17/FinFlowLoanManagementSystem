[README.md](https://github.com/user-attachments/files/26278017/README.md)
# 🏦 FinFlow Loan Management System

A **microservices-based Loan Management System** built with Spring Boot, designed to handle the complete loan lifecycle — from user registration and loan application to admin decision-making and document verification.

---

## 📐 Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │  ← Single entry point (Port 8080)
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
  ┌───────▼──────┐    ┌──────────▼───────┐    ┌────────▼────────┐
  │ Auth Service │    │Application Service│    │  Admin Service  │
  │  (Port 8081) │    │   (Port 8082)     │    │  (Port 8083)    │
  └──────────────┘    └──────────────────┘    └─────────────────┘
                                 │
                        ┌────────▼────────┐
                        │Document Service │
                        │  (Port 8084)    │
                        └─────────────────┘

  ┌──────────────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐
  │ Service Registry │   │  MySQL   │   │ RabbitMQ │   │ Zipkin  │
  │  Eureka (8761)   │   │  (3306)  │   │  (5672)  │   │ (9411)  │
  └──────────────────┘   └──────────┘   └──────────┘   └─────────┘
```

---

## 🧩 Microservices

| Service | Port | Responsibility |
|---|---|---|
| **API Gateway** | 8080 | Routes all requests, JWT validation |
| **Auth Service** | 8081 | User registration, login, JWT generation |
| **Application Service** | 8082 | Loan application submission & tracking |
| **Admin Service** | 8083 | Loan review, approval/rejection decisions |
| **Document Service** | 8084 | Document upload & verification (Aadhaar, PAN, Income Certificate) |
| **Service Registry** | 8761 | Eureka service discovery |

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3
- **Security:** Spring Security, JWT (JSON Web Tokens)
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Messaging:** RabbitMQ (async status updates)
- **Distributed Tracing:** Zipkin + Micrometer
- **Database:** MySQL
- **ORM:** Spring Data JPA / Hibernate
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Containerization:** Docker, Docker Compose
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **Build Tool:** Maven

---

## ✨ Features

### 👤 Authentication
- User registration and login
- JWT-based stateless authentication
- Role-based access (`APPLICANT`, `ADMIN`)

### 📋 Loan Application
- Submit loan applications
- Track application status in real-time
- Async status notifications via RabbitMQ

### 📁 Document Management
- Upload mandatory documents: **Aadhaar**, **PAN Card**, **Income Certificate**
- Type-safe uploads using enums (prevents invalid document types)
- Admin can view and download applicant documents
- Document completeness tracking

### 🔐 Admin Panel
- View all loan applications
- Approve or reject applications with remarks
- View applicant documents for verification
- Generate reports and decisions

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8+
- Docker & Docker Compose
- RabbitMQ (or via Docker)

### 1. Clone the Repository
```bash
git clone https://github.com/gillelakoushikreddy-17/FinFlowLoanManagementSystem.git
cd FinFlowLoanManagementSystem
```

### 2. Configure the Database
Create a MySQL database for each service:
```sql
CREATE DATABASE auth_db;
CREATE DATABASE application_db;
CREATE DATABASE admin_db;
CREATE DATABASE document_db;
```
Update credentials in each service's `application.yml` if needed.

### 3. Run with Docker Compose
```bash
docker-compose up --build
```

### 4. Run Services Manually (in order)
```bash
# 1. Service Registry (Eureka)
cd service-registry && mvn spring-boot:run

# 2. Auth Service
cd auth-service && mvn spring-boot:run

# 3. Application Service
cd application-service && mvn spring-boot:run

# 4. Admin Service
cd admin-service && mvn spring-boot:run

# 5. Document Service
cd document-service && mvn spring-boot:run

# 6. API Gateway (last)
cd api-gateway && mvn spring-boot:run
```

---

## 📡 API Endpoints

### Auth Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Application Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/applications` | Submit a loan application |
| GET | `/api/applications/{id}` | Get application by ID |
| GET | `/api/applications` | Get all applications (Admin) |

### Document Service
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/documents/upload` | Upload a document |
| GET | `/api/documents/{applicantId}` | Get documents for an applicant |

### Admin Service
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/applications` | View all loan applications |
| POST | `/api/admin/decision` | Approve or reject an application |
| GET | `/api/admin/documents/{applicantId}` | View applicant documents |

---

## 🔑 Authentication

All protected endpoints require a JWT token in the request header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 🧪 Running Tests

```bash
# Run all tests for a specific service
cd auth-service
mvn test

# Run all tests across all services
mvn test --projects auth-service,application-service,admin-service,document-service
```

---

## 📊 Monitoring

| Tool | URL | Description |
|---|---|---|
| Eureka Dashboard | http://localhost:8761 | Service registry UI |
| Zipkin UI | http://localhost:9411 | Distributed tracing |
| RabbitMQ Management | http://localhost:15672 | Message queue UI |
| Swagger UI (Auth) | http://localhost:8081/swagger-ui.html | API docs |

---

## 📁 Project Structure

```
FinFlowLoanManagementSystem/
├── api-gateway/
├── auth-service/
├── application-service/
├── admin-service/
├── document-service/
├── service-registry/
├── docker-compose.yml
└── pom.xml  (parent POM)
```

---

## 👨‍💻 Author

**Gillela Koushik Reddy**
- GitHub: [@gillelakoushikreddy-17](https://github.com/gillelakoushikreddy-17)

---

## 📄 License

This project is for educational purposes.
