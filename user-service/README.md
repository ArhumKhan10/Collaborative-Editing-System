# User Management Service

Microservice for user authentication, registration, and profile management.

## Port

**8081**

## Endpoints

### Authentication

#### Register User
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "507f1f77bcf86cd799439011",
  "username": "john_doe",
  "email": "john@example.com"
}
```

### Profile Management

#### Get User Profile
```http
GET /api/users/profile/{userId}
Authorization: Bearer {token}
```

#### Update User Profile
```http
PUT /api/users/profile/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "john_doe_updated",
  "avatar": "https://example.com/avatar.jpg"
}
```

#### Change Password
```http
PUT /api/users/{userId}/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "password123",
  "newPassword": "newpassword456"
}
```

### Utility Endpoints

#### Check if Email Exists
```http
GET /api/users/exists/email/{email}
```

#### Check if Username Exists
```http
GET /api/users/exists/username/{username}
```

## API Documentation

Swagger UI: http://localhost:8081/swagger-ui.html

OpenAPI JSON: http://localhost:8081/api-docs

## Running the Service

```bash
# From user-service directory
mvn spring-boot:run

# Or from parent directory
cd user-service && mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

## Health Check

```http
GET http://localhost:8081/actuator/health
```

## Configuration

- MongoDB: localhost:27017/collab_edit_db
- JWT Secret: Must match other services
- JWT Expiration: 24 hours
- Password Hashing: BCrypt (strength 12)

## Dependencies

- Spring Boot Web
- Spring Data MongoDB
- Spring Security
- JWT (jjwt)
- Springdoc OpenAPI (Swagger)
- Lombok
- Common Library

## Testing

JUnit 5 tests with Mockito for:
- User registration (success, duplicate email, duplicate username)
- User login (success, invalid credentials)
- Profile management (get, update)
- Password validation
- Existence checks
