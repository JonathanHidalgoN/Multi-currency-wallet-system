# PayFlow - Multi-Currency Wallet & Transaction System

A professional Spring Boot backend for a multi-currency wallet and transaction management system with JWT authentication.

## Architecture Overview

The application follows a layered architecture pattern:

```
Controllers → Services → Repositories → Database
```

## Tech Stack

- **Framework**: Spring Boot 3.3.4
- **Java Version**: Java 21
- **Database**: PostgreSQL 16
- **Authentication**: JWT (JSON Web Tokens) with Spring Security
- **Password Hashing**: BCrypt
- **ORM**: Hibernate with Spring Data JPA
- **Database Migrations**: Flyway
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito, Spring Security Test

## Project Structure

```
wallet-api/
├── src/main/java/com/payflow/
│   ├── controller/           # REST endpoints
│   ├── services/             # Business logic
│   ├── repository/           # Data access
│   ├── entity/               # Domain entities
│   ├── DTOS/                 # Request/Response objects
│   ├── value/                # Value objects (Money)
│   ├── security/             # JWT authentication
│   ├── config/
│   ├── exception/            # Global error handling
│   ├── validation/
│   └── util/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/
├── src/test/java/com/payflow/
│   ├── services/
│   ├── entity/
│   └── value/
├── pom.xml
├── .env
├── docker-compose.yml
└── .gitignore
```

## Security Implementation

### JWT Authentication Flow

1. **User Registration** (`POST /api/auth/register`)
   - Accepts email, password, fullName
   - Password is hashed using BCrypt
   - Wallet is automatically created for the user

2. **User Login** (`POST /api/auth/login`)
   - Accepts email and password
   - Password is verified using BCrypt password encoder
   - On successful authentication, JWT token is generated with user ID
   - Token is returned in response

3. **Protected Endpoints**
   - All endpoints except `/api/auth/register` and `/api/auth/login` require valid JWT
   - Token must be sent in `Authorization` header: `Bearer <token>`
   - JwtAuthenticationFilter validates token on each request

### Key Security Classes

#### JwtTokenProvider (com.payflow.security)
- Generates JWT tokens with HS256 algorithm
- Validates JWT tokens
- Extracts user ID from token claims
- Uses minimum 32-character secret key (HS256 requirement)

#### JwtAuthenticationFilter (com.payflow.security)
- OncePerRequestFilter that validates JWT on each request
- Extracts Bearer token from Authorization header
- Sets Authentication object in SecurityContext if token is valid
- Silently continues if token is invalid (Spring Security will reject later)

#### SecurityConfig (com.payflow.config)
- Defines PasswordEncoder bean (BCryptPasswordEncoder)
- Configures CORS for frontend applications (localhost:3000, localhost:5173)
- Sets up JWT authentication filter in security chain
- Allows public access to `/api/auth/register` and `/api/auth/login`
- Requires authentication for all other endpoints

### Exception Handling

GlobalExceptionHandler provides centralized exception handling:
- **MethodArgumentNotValidException**: Validation errors (400 Bad Request)
- **IllegalArgumentException**: Authentication failures, user not found (401 Unauthorized)
- **RuntimeException**: Business logic errors (400 Bad Request)
- **Exception**: All other errors (500 Internal Server Error)

## Setup & Running

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- WSL (for Windows users)

### Environment Setup

1. **Clone or navigate to project directory**
   ```bash
   cd /home/jonathan-linux/jonas/projects/walletTransactionSystemJava/wallet-api
   ```

2. **Create .env file** (already provided)
   ```
   SPRING_PROFILES_ACTIVE=dev
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/wallet_db
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=postgres
   JWT_SECRET=your-very-long-secret-key-at-least-32-characters-long
   JWT_EXPIRATION=86400000
   ```

3. **Start PostgreSQL with Docker**
   ```bash
   docker-compose up -d
   ```

4. **Build the application**
   ```bash
   mvn clean package
   ```

5. **Run the application**
   ```bash
   java -jar target/wallet-api-1.0-SNAPSHOT.jar
   ```

   Or using Maven:
   ```bash
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080`

### Swagger/OpenAPI Documentation

After starting the application, access API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Wallet

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallets/me` | Get wallet details |
| GET | `/api/wallets/me/balance?currency=USD` | Get balance for currency |
| GET | `/api/wallets/me/balances` | Get all balances |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/deposit` | Deposit money |
| POST | `/api/transactions/withdraw` | Withdraw money |
| POST | `/api/transactions/transfer` | Transfer to another user |
| GET | `/api/transactions/history` | Get transaction history (paginated) |

## Example Requests

### 1. Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePassword123",
    "fullName": "John Doe"
  }'
```

Response:
```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "message": "User registered successfully"
}
```

### 2. Login User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePassword123"
  }'
```

Response:
```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzAwNjAwMDAwLCJleHAiOjE3MDA2ODYwMDB9.signature...",
  "message": "Login successful"
}
```

### 3. Get Wallet Info (Requires JWT)

```bash
curl -X GET http://localhost:8080/api/wallets/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Deposit Money

```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "currency": "USD"
  }'
```

### 5. Transfer Money

```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientUserId": 2,
    "senderCurrency": "USD",
    "recipientCurrency": "EUR",
    "amount": 100
  }'
```

## Next Steps

1. Implement comprehensive unit and integration tests
2. Add rate limiting for sensitive endpoints
3. Implement refresh tokens for better security
4. Add audit logging for transactions
5. Implement email verification for registration
6. Add two-factor authentication
7. Create admin dashboard for transaction monitoring
