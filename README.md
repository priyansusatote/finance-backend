# 💰 Finance Data Processing and Access Control Backend

> A production-ready backend system for managing financial records with stateless JWT authentication, role-based access control (RBAC), and dashboard analytics — built with Spring Boot 4 and Java 21.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Core Features](#core-features)
- [Security Design](#security-design)
- [API Reference](#api-reference)
- [Role-Based Access Matrix](#role-based-access-matrix)
- [Key Design Decisions](#key-design-decisions)
- [Data Model](#data-model)
- [Error Handling](#error-handling)
- [Setup & Running Locally](#setup--running-locally)
- [Future Improvements](#future-improvements)

---

## Overview

This project is a backend REST API designed to handle **financial record management** with secure, role-based access control. It demonstrates clean backend architecture principles:

- **Stateless authentication** using JWT (no session storage)
- **Method-level security** with Spring's `@PreAuthorize`
- **Layered architecture** with clear separation of concerns
- **DTO-based API contracts** to decouple internal models from external responses
- **Soft delete** to preserve financial data history
- **Precision-safe arithmetic** using `BigDecimal` for all monetary values

This project is intended to showcase backend engineering skills including API design, security implementation, and proper handling of financial data.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI 3.0.2 (Swagger UI) |
| Boilerplate Reduction | Lombok |
| Build Tool | Maven |

---

## Architecture

The application follows a strict **layered architecture** to ensure separation of concerns and testability:

```
┌─────────────────────────────────────────────────┐
│              HTTP Request (Client)               │
└──────────────────────┬──────────────────────────┘
                       │
              ┌────────▼────────┐
              │  JWT Filter     │  ← Validates token, sets SecurityContext
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │   Controller    │  ← Routes requests, validates input DTOs
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │    Service      │  ← Business logic, orchestration
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │   Repository    │  ← JPA data access (Spring Data)
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │   PostgreSQL    │  ← Persistent storage
              └─────────────────┘
```

**Request Lifecycle:**
1. `JwtAuthenticationFilter` intercepts every request, validates `Bearer` token, extracts `email` and `role`, and populates the `SecurityContext`
2. Spring Security evaluates `@PreAuthorize` annotations based on the authenticated role
3. Controller deserializes and validates the request DTO
4. Service executes business logic and maps entities ↔ DTOs
5. Repository executes the database query via JPA

---

## Project Structure

```
src/main/java/com/priyansu/finance_backend/
│
├── controller/
│   ├── AuthController.java            # POST /auth/login
│   ├── UserController.java            # User management endpoints
│   ├── FinancialRecordController.java # Financial record CRUD
│   └── DashboardController.java       # Analytics summary
│
├── service/
│   ├── AuthService.java               # Interface
│   ├── UserService.java               # Interface
│   ├── FinancialRecordService.java    # Interface
│   ├── DashboardService.java          # Interface
│   └── Impl/                          # Implementations
│
├── repository/
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java
│
├── entity/
│   ├── User.java                      # users table
│   └── FinancialRecord.java           # financial_records table
│
├── dto/
│   ├── LoginRequest.java
│   ├── CreateUserRequest.java
│   ├── UserResponse.java
│   ├── FinancialRecordRequest.java
│   ├── FinancialRecordResponse.java
│   └── DashboardResponse.java
│
├── security/
│   ├── JwtService.java                # Token generation, validation, claim extraction
│   └── JwtAuthenticationFilter.java   # OncePerRequestFilter JWT interceptor
│
├── exception/
│   ├── GlobalExceptionHandler.java    # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── DuplicateResourceException.java
│
├── enums/
│   ├── Role.java                      # VIEWER, ANALYST, ADMIN
│   ├── RecordType.java                # INCOME, EXPENSE
│   └── UserStatus.java                # ACTIVE, INACTIVE
│
└── config/
    ├── SecurityConfig.java
    └── OpenApiConfig.java
```

---

## Core Features

### 1. Authentication
- `POST /auth/login` → validates credentials, returns signed **JWT token**
- Token contains: `userId`, `email`, `role` as claims
- Token validity: **1 hour** (configurable via `jwt.expiration`)
- Token signed using **HMAC-SHA** with a Base64-encoded secret

### 2. User Management
| Action | Endpoint | Access |
|---|---|---|
| Register | `POST /users` | Public |
| List All Users | `GET /users` | ADMIN |
| Update Status | `PATCH /users/{id}/status` | ADMIN |
| Update Role | `PATCH /users/{id}/role` | ADMIN |

- New users are assigned `ROLE_VIEWER` by default
- Admin can promote users to `ANALYST` or `ADMIN`
- Admin can deactivate users by setting status to `INACTIVE`

### 3. Financial Records
| Action | Endpoint | Access |
|---|---|---|
| Create Record | `POST /records` | ADMIN |
| View All Records | `GET /records` | ADMIN, ANALYST |
| Delete Record | `DELETE /records/{id}` | ADMIN |

- Each record stores: `amount` (BigDecimal), `type` (INCOME/EXPENSE), `category`, `date`, `note`, `createdBy`
- Delete is **soft delete** — sets a `deleted` flag, the record remains in DB

### 4. Dashboard Analytics
| Action | Endpoint | Access |
|---|---|---|
| Financial Summary | `GET /dashboard/summary` | ADMIN, ANALYST |
| Auth Debug | `GET /dashboard/whoami` | Any authenticated |

Dashboard response includes:
- **Total Income** & **Total Expense** (aggregated)
- **Net Balance** (`income − expense`)
- **Category-wise breakdown** (e.g., `{ "salary": 50000, "food": 8000 }`)
- **5 most recent transactions** (sorted by date descending)

---

## Security Design

### JWT Authentication Flow

```
Client                          Server
  │                               │
  │── POST /auth/login ──────────▶│
  │   { email, password }         │── BCrypt verify password
  │                               │── Generate JWT (userId, email, role)
  │◀── 200 OK: "eyJhbGci..." ────│
  │                               │
  │── GET /records ──────────────▶│
  │   Authorization: Bearer <token>│
  │                               │── JwtAuthenticationFilter
  │                               │   ├── Validate signature + expiry
  │                               │   ├── Extract email + role
  │                               │   └── Set SecurityContext
  │                               │── @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
  │◀── 200 OK: [...records] ─────│
```

### `JwtAuthenticationFilter` (extends `OncePerRequestFilter`)

```java
// Extracts Bearer token → validates → sets authentication in SecurityContext
String token = authHeader.substring(7);
jwtService.validateToken(token);             // checks signature + expiry
String role = jwtService.extractRole(token); // extracts claim
SecurityContextHolder.getContext()
    .setAuthentication(new UsernamePasswordAuthenticationToken(email, null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
```

### Password Security
- Passwords hashed with **BCrypt** before storage
- Verification uses `passwordEncoder.matches(rawPassword, hashedPassword)` — no plaintext comparison ever

---

## API Reference

### Auth

```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "secret123"
}

→ 200 OK: "eyJhbGciOiJIUzI1NiJ9..."
```

### Users

```http
POST /users                          # Public signup
GET  /users                          # Admin: list all users
PATCH /users/{id}/status?status=INACTIVE  # Admin: deactivate user
PATCH /users/{id}/role?role=ANALYST       # Admin: change role
```

### Financial Records

```http
POST /records                        # Admin: create record
{
  "amount": 15000.00,
  "type": "INCOME",
  "category": "salary",
  "date": "2025-04-01",
  "note": "April salary"
}

GET    /records                      # Admin + Analyst: view all
DELETE /records/{id}                 # Admin: soft delete
```

### Dashboard

```http
GET /dashboard/summary               # Admin + Analyst
→ {
    "totalIncome": 50000.00,
    "totalExpense": 12000.00,
    "netBalance": 38000.00,
    "categoryBreakdown": { "salary": 50000, "food": 8000, "rent": 4000 },
    "recentTransactions": [...]
  }
```

> 📖 Full interactive API docs available at: `http://localhost:8080/swagger-ui.html`

---

## Role-Based Access Matrix

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| `POST /auth/login` | ✅ | ✅ | ✅ |
| `POST /users` | ✅ | ✅ | ✅ |
| `GET /users` | ❌ | ❌ | ✅ |
| `PATCH /users/{id}/status` | ❌ | ❌ | ✅ |
| `PATCH /users/{id}/role` | ❌ | ❌ | ✅ |
| `POST /records` | ❌ | ❌ | ✅ |
| `GET /records` | ❌ | ✅ | ✅ |
| `DELETE /records/{id}` | ❌ | ❌ | ✅ |
| `GET /dashboard/summary` | ❌ | ✅ | ✅ |

Access control is enforced at the **method level** using `@PreAuthorize`, not just at the URL level — making it impossible to bypass via URL manipulation.

---

## Key Design Decisions

### 1. `BigDecimal` for Financial Amounts
```java
private BigDecimal amount; // NOT double or float
```
`double` and `float` use IEEE 754 floating-point, which causes precision loss (e.g., `0.1 + 0.2 ≠ 0.3`). Financial systems require **exact decimal arithmetic** — `BigDecimal` guarantees this.

### 2. DTO Layer for API Contracts
Entities are never exposed directly in API responses. DTOs (`FinancialRecordRequest`, `FinancialRecordResponse`, `UserResponse`) decouple the internal data model from the API contract — allowing the schema to evolve without breaking clients, and preventing accidental exposure of sensitive fields like `password`.

### 3. Stateless JWT Authentication
No server-side sessions or databases are used to track login state. Each request is self-contained — the JWT carries the user's identity and role. This makes the application **horizontally scalable** by default (any server instance can validate any token).

### 4. Method-Level Security with `@PreAuthorize`
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<FinancialRecordResponse> create(...) { ... }
```
Security is enforced closest to the business logic, not just at routing level. This prevents privilege escalation even if routing configurations are misconfigured.

### 5. Soft Delete Instead of Hard Delete
```java
private boolean deleted = false; // flag on FinancialRecord
```
Financial records should never be permanently erased. Soft delete maintains a complete audit history, enables data recovery, and supports future audit logging features. Queries automatically filter out deleted records.

### 6. Service Interface → Implementation Pattern
All services define an interface (`UserService`, `FinancialRecordService`, etc.) with a separate `Impl` class. This enables easy mocking in unit tests, supports future dependency injection swaps, and follows the **Dependency Inversion Principle**.

### 7. Global Exception Handler via `@RestControllerAdvice`
```java
@ExceptionHandler(ResourceNotFoundException.class) → 404
@ExceptionHandler(DuplicateResourceException.class) → 409 CONFLICT
@ExceptionHandler(BadRequestException.class)        → 400
@ExceptionHandler(MethodArgumentNotValidException.class) → field-level validation errors
```
Centralizes all exception handling, ensures consistent error response format, and prevents stack traces from leaking in API responses.

---

## Data Model

### `users` table

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT` (PK) | Auto-generated |
| `name` | `VARCHAR` | |
| `email` | `VARCHAR` (UNIQUE) | Login identifier |
| `password` | `VARCHAR` | BCrypt hashed |
| `role` | `ENUM` | `VIEWER`, `ANALYST`, `ADMIN` |
| `status` | `ENUM` | `ACTIVE`, `INACTIVE` |

### `financial_records` table

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT` (PK) | Auto-generated |
| `amount` | `DECIMAL` | BigDecimal precision |
| `type` | `ENUM` | `INCOME`, `EXPENSE` |
| `category` | `VARCHAR` | e.g., `salary`, `food` |
| `date` | `DATE` | Transaction date |
| `note` | `VARCHAR` | Optional description |
| `deleted` | `BOOLEAN` | Soft delete flag |
| `user_id` | `BIGINT` (FK) | References `users.id` |

---

## Setup & Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 14+

### 1. Clone the Repository
```bash
git clone https://github.com/priyansusatote/finance-backend.git
cd finance-backend
```

### 2. Create PostgreSQL Database
```sql
CREATE DATABASE finance_db;
```

### 3. Configure `application.yaml`
The app reads database credentials from `application.yaml`. Update if needed:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_db
    username: postgres
    password: your_password
```

### 4. Set Environment Variable
```bash
# Windows (PowerShell)
$env:JWT_SECRET = "your-base64-encoded-256-bit-secret"

# Linux / macOS
export JWT_SECRET=your-base64-encoded-256-bit-secret
```

### 5. Run the Application
```bash
./mvnw spring-boot:run
```
App starts on `http://localhost:8080`

### 6. Explore the API
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Postman**: Import and hit `POST /auth/login` first, then use the returned JWT as `Authorization: Bearer <token>` for all subsequent requests.

### Quick Test Flow
```
1. POST /users          → Register as viewer (auto-assigned VIEWER role)
2. POST /auth/login     → Get JWT token
3. PATCH /users/{id}/role?role=ADMIN  → (needs Admin token) Promote to Admin
4. POST /records        → Create a financial record
5. GET /dashboard/summary → View analytics
```

---

## Future Improvements

| Item | Description |
|---|---|
| Refresh Tokens | Add refresh token endpoint to extend sessions without re-login |
| Pagination | Add `Pageable` support on `GET /records` for large datasets |
| DB-level Aggregation | Move dashboard aggregation from in-memory to SQL (`SUM`, `GROUP BY`) |
| Audit Logging | Log who created/modified/deleted each record with timestamps |
| Per-User Data Isolation | Each user sees only their own records (currently global) |
| Unit & Integration Tests | Add JUnit + Mockito tests for service layer and controllers |
| Rate Limiting | Add API rate limiting to prevent abuse |

---

## Author

**Priyansu**  
Backend Developer · Java · Spring Boot · PostgreSQL  
🔗 [github.com/priyansusatote](https://github.com/priyansusatote)  
📁 [Project Repository](https://github.com/priyansusatote/finance-backend)

---

*Built with a focus on clean code, secure design, and maintainable backend architecture.*
