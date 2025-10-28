# LEXIA Backend API

**LEXIA** - AI-Powered English Learning Platform for Working Professionals

## 🚀 Overview

LEXIA Backend is a Spring Boot 3.x REST API providing authentication, user profile management, and AI-powered English learning features. Built with security-first principles and comprehensive API documentation.

## 📚 Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL 17.6
- **Security**: JWT (JSON Web Tokens) with BCrypt password hashing
- **Migration**: Flyway
- **Testing**: JUnit 5, Mockito
- **API Documentation**: SpringDoc OpenAPI 3.0 (Swagger)
- **Build Tool**: Gradle 8.x

## 🔧 Prerequisites

- Java 17 or higher
- PostgreSQL 17.x
- Gradle 8.x (or use included wrapper)

## ⚙️ Setup Instructions

### 1. Database Configuration

Create a PostgreSQL database named `lexia`:

```sql
CREATE DATABASE lexia;
```

### 2. Application Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lexia
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. JWT Secret Configuration

Set your JWT secret key in `application.properties`:

```properties
jwt.secret=your-secure-secret-key-here
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

### 4. Build and Run

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Start the application
./gradlew bootRun
```

The application will start on `http://localhost:8088`

## 📖 API Documentation

### Swagger UI (Interactive Documentation)

Access the interactive API documentation at:

**URL**: [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)

#### Features:

- ✅ **Interactive Testing**: Try out API endpoints directly from the browser
- ✅ **Request/Response Examples**: See realistic JSON examples for all operations
- ✅ **Authentication**: Built-in JWT token authorization
- ✅ **Validation Details**: View all validation constraints and error responses
- ✅ **Schema Documentation**: Comprehensive field descriptions for all DTOs

### OpenAPI Specification

Download the OpenAPI 3.0 specification:

**JSON Format**: [http://localhost:8088/api-docs](http://localhost:8088/api-docs)

**YAML Format**: [http://localhost:8088/api-docs.yaml](http://localhost:8088/api-docs.yaml)

Use these specifications with tools like:

- Postman (Import OpenAPI spec)
- Insomnia
- API client generators
- Documentation portals

## 🔐 Authentication

### How to Use JWT Authentication in Swagger UI

1. **Register a New User**

   - Navigate to **Authentication API** section
   - Expand `POST /api/v1/auth/register`
   - Click "Try it out"
   - Fill in the request body:
     ```json
     {
       "email": "test@lexia.com",
       "password": "SecurePass123",
       "confirmPassword": "SecurePass123",
       "fullName": "Test User"
     }
     ```
   - Click "Execute"
   - You should receive a `201 Created` response

2. **Login to Get JWT Token**

   - Expand `POST /api/v1/auth/login`
   - Click "Try it out"
   - Enter credentials:
     ```json
     {
       "email": "test@lexia.com",
       "password": "SecurePass123"
     }
     ```
   - Click "Execute"
   - Copy the `accessToken` from the response

3. **Authorize Swagger UI**

   - Click the **"Authorize"** button (lock icon) at the top right
   - In the dialog, enter: `Bearer YOUR_ACCESS_TOKEN`
   - Click "Authorize"
   - Click "Close"

4. **Access Protected Endpoints**
   - Now you can test any protected endpoint (User Profile API)
   - All requests will automatically include your JWT token

### Token Lifecycle

- **Access Token**: Valid for 15 minutes (900,000 ms)
- **Refresh Token**: Valid for 7 days (604,800,000 ms)

Use `POST /api/v1/auth/refresh` to get a new access token without re-authentication.

## 📡 API Endpoints

### Authentication API

| Method | Endpoint                | Description              | Auth Required |
| ------ | ----------------------- | ------------------------ | ------------- |
| POST   | `/api/v1/auth/register` | Register new user        | ❌ No         |
| POST   | `/api/v1/auth/login`    | Login and get JWT tokens | ❌ No         |
| POST   | `/api/v1/auth/refresh`  | Refresh access token     | ❌ No         |

### User Profile API

| Method | Endpoint                       | Description              | Auth Required |
| ------ | ------------------------------ | ------------------------ | ------------- |
| GET    | `/api/v1/users/profile`        | Get current user profile | ✅ Yes (JWT)  |
| PUT    | `/api/v1/users/profile`        | Update user profile      | ✅ Yes (JWT)  |
| POST   | `/api/v1/users/profile/avatar` | Update avatar URL        | ✅ Yes (JWT)  |
| DELETE | `/api/v1/users/profile/avatar` | Remove avatar            | ✅ Yes (JWT)  |

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### View Test Coverage Report

```bash
./gradlew jacocoTestReport
```

Open `build/reports/jacoco/test/html/index.html` in your browser.

**Current Coverage**: 81% (exceeds 70% target ✅)

### Test Categories

- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller endpoints with security
- **Validation Tests**: Input validation and error handling
- **Security Tests**: JWT authentication and authorization

## 🏗️ Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/lexia/backend/
│   │   │   ├── auth/              # JWT authentication
│   │   │   ├── common/            # Error handling, utilities
│   │   │   ├── config/            # Spring configuration
│   │   │   ├── controller/        # REST controllers
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── entity/            # JPA entities
│   │   │   ├── exception/         # Custom exceptions
│   │   │   ├── mapper/            # Entity-DTO mappers
│   │   │   ├── repository/        # JPA repositories
│   │   │   ├── security/          # Security configuration
│   │   │   ├── service/           # Business logic
│   │   │   └── validation/        # Custom validators
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/      # Flyway migrations
│   └── test/                      # Unit and integration tests
├── docs/                          # Project documentation
├── build.gradle                   # Gradle build configuration
└── README.md                      # This file
```

## 🔒 Security Features

- ✅ **BCrypt Password Hashing**: Cost factor 12
- ✅ **JWT Token Authentication**: Secure token-based auth
- ✅ **Token Rotation**: Refresh token rotation with family tracking
- ✅ **Theft Detection**: Automatic detection of stolen refresh tokens
- ✅ **Input Validation**: Comprehensive validation on all inputs
- ✅ **Audit Logging**: Track all user profile changes
- ✅ **CORS Configuration**: Secure cross-origin requests
- ✅ **SQL Injection Prevention**: JPA parameterized queries

## 📊 Database Schema

### Core Tables

- **users**: User accounts and authentication
- **user_profiles**: Extended user profile information
- **refresh_tokens**: Refresh token management with family tracking
- **roles**: User roles (STUDENT, INSTRUCTOR, ADMIN)
- **user_roles**: User-role associations
- **audit_logs**: Audit trail for compliance

See `docs/context/DATABASE-SCHEMA.md` for detailed schema documentation.

## 🚦 Health Checks

- **Application Health**: Spring Boot Actuator (if enabled)
- **Database Connection**: Flyway migration validation on startup
- **H2 Console**: Available at `/h2-console` (development only)

## 📝 Environment Variables

For production deployment, use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/lexia
export SPRING_DATASOURCE_USERNAME=username
export SPRING_DATASOURCE_PASSWORD=password
export JWT_SECRET=your-production-secret-key
export JWT_EXPIRATION=900000
export JWT_REFRESH_EXPIRATION=604800000
```

## 🤝 Contributing

1. Follow the code standards in `docs/context/CODE-STANDARDS.md`
2. Write unit tests for all new features (minimum 70% coverage)
3. Update API documentation with Swagger annotations
4. Run all tests before submitting PR: `./gradlew test`

## 📄 License

MIT License - See LICENSE file for details

## 📞 Support

For issues and questions:

- Email: support@lexia.com
- Documentation: `docs/` directory
- API Issues: Create GitHub issue with Swagger examples

## 🔄 Version History

- **v1.0.0** (Sprint 1) - Authentication & User Profile Management
  - User registration and login
  - JWT token authentication with refresh
  - User profile CRUD operations
  - Comprehensive API documentation
  - 81% test coverage

---

**Built with ❤️ by the LEXIA Team**
