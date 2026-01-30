# Common Library

Shared utilities, DTOs, exceptions, and configurations used across all microservices.

## Contents

### DTOs (Data Transfer Objects)
- `ApiResponse<T>` - Standardized API response wrapper
- `ErrorResponse` - Standardized error response

### Exceptions
- `ResourceNotFoundException` - 404 errors
- `UnauthorizedException` - 401 errors
- `BadRequestException` - 400 errors
- `DuplicateResourceException` - 409 errors

### Utilities
- `JwtUtil` - JWT token generation and validation

### Configuration
- `CorsConfig` - CORS configuration for all services

## Usage

Add as dependency in other services:

```xml
<dependency>
    <groupId>com.collab</groupId>
    <artifactId>common-lib</artifactId>
</dependency>
```

## JWT Configuration

All services must use the same JWT secret:

```yaml
jwt:
  secret: collab-edit-secret-key-must-be-at-least-256-bits-for-HS256-algorithm
  expiration: 86400000  # 24 hours
```

**IMPORTANT:** This secret must be identical across all microservices for JWT validation to work correctly.
