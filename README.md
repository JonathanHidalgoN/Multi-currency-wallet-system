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
│   ├── config/               # Configuration classes
│   ├── exception/            # Global error handling
│   ├── filter/               # Request filters
│   ├── iterceptor/           # Request interceptors
│   ├── validation/           # Custom validators
│   ├── util/                 # Utility classes
│   ├── RateLimitService.java # Rate limiting service
│   └── App.java              # Main application class
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

### Exception Handling

GlobalExceptionHandler provides centralized exception handling:
- **MethodArgumentNotValidException**: Validation errors (400 Bad Request)

## Key Features

- **Database Locking** (`src/main/java/com/payflow/services/WalletService.java`): Implements pessimistic locking for thread-safe wallet operations during concurrent transactions
- **Rate Limiting** (`src/main/java/com/payflow/RateLimitService.java`): Token bucket-based rate limiting using Bucket4j for both IP and user-level request throttling

## Setup & Running

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- WSL (for Windows users)

### Environment Setup

1. **Clone or navigate to project directory**
   ```bash
   cd /{your-path}/wallet-api
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

3. **Start the application with Docker**
   ```bash
   docker-compose up --build -d
   ```
   ```

   The API will be available at `http://localhost:8080`

### Swagger/OpenAPI Documentation

After starting the application, access API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Running Tests

```bash
mvn test
```

**Note:** Integration tests use Testcontainers (requires Docker running). If tests fail with `client version 1.32 is too old`, add to `~/.docker-java.properties`:
```
api.version=1.44
```
See: https://github.com/testcontainers/testcontainers-java/issues/11212#issuecomment-3516573631

## Next Steps

1. Implement refresh tokens for better security
2. Add audit logging for transactions
3. Implement email verification for registration
4. Add two-factor authentication
5. Create admin dashboard for transaction monitoring
