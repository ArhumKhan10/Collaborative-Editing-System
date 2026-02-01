# Collaborative Editing System

A microservices-based collaborative document editing platform (similar to Google Docs/Overleaf) built with Java Spring Boot backend and React frontend.

## Architecture

```
┌─────────────┐
│   React     │
│  Frontend   │
│  (:3000)    │
└──────┬──────┘
       │ HTTP/WebSocket
       │
┌──────▼──────────────────────────────────────┐
│         API Gateway (:8080)                  │
│  (Routing, JWT Validation, CORS)            │
└───┬─────────┬──────────┬──────────┬─────────┘
    │         │          │          │
    │         │          │          │
┌───▼────┐ ┌──▼──────┐ ┌▼─────────┐ ┌▼────────┐
│  User  │ │Document │ │ Version  │ │WebSocket│
│Service │ │ Service │ │ Service  │ │  Direct │
│ :8081  │ │  :8082  │ │  :8083   │ │  :8082  │
└───┬────┘ └──┬──────┘ └┬─────────┘ └─────────┘
    │         │          │
    └─────────┴──────────┴─────────┐
                                   │
                          ┌────────▼─────────┐
                          │    MongoDB       │
                          │   (:27017)       │
                          │ collab_edit_db   │
                          └──────────────────┘
```

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5** (Web, Data MongoDB, Security, WebSocket, Actuator)
- **Spring Cloud Gateway 2022.0.4**
- **MongoDB** (Document database)
- **JWT** for authentication
- **JUnit 5 + Mockito** for testing
- **Swagger/OpenAPI** for API documentation
- **Maven** for build management

### Frontend
- **React 18** with Hooks
- **Material-UI (MUI) v5**
- **React Router v6**
- **Axios** for HTTP requests
- **SockJS + STOMP** for WebSocket
- **React-Quill** for rich text editing
- **Vite** for fast development

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- MongoDB 6.0+ (running on localhost:27017)

## Quick Start

### Option 1: Run Everything with Scripts

**Windows:**
```bash
# Start all backend services
start-backend.bat

# In another terminal, start frontend
start-frontend.bat
```

### Option 2: Manual Start

**1. Build all services:**
```bash
mvn clean install
```

**2. Start services in order:**

```bash
# Terminal 1: API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2: User Service
cd user-service
mvn spring-boot:run

# Terminal 3: Document Service
cd document-service
mvn spring-boot:run

# Terminal 4: Version Service
cd version-service
mvn spring-boot:run

# Terminal 5: React Frontend
cd frontend
npm install
npm run dev
```

**3. Access the application:**
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- User Service Swagger: http://localhost:8081/swagger-ui.html
- Document Service Swagger: http://localhost:8082/swagger-ui.html
- Version Service Swagger: http://localhost:8083/swagger-ui.html


## Microservices

### 1. User Management Service (:8081)
**Operations:**
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Authenticate user (returns JWT)
- `GET /api/users/profile/{userId}` - Get user profile
- `PUT /api/users/profile/{userId}` - Update user profile
- `GET /api/users/{userId}/documents` - List user's documents
- `PUT /api/users/{userId}/password` - Change password

### 2. Document Editing Service (:8082)
**Operations:**
- `POST /api/documents` - Create new document
- `GET /api/documents/{documentId}` - Get document
- `PUT /api/documents/{documentId}` - Update document
- `POST /api/documents/{documentId}/share` - Share with collaborators
- `DELETE /api/documents/{documentId}` - Delete document
- `GET /api/documents` - List accessible documents
- `WS /ws/document/{documentId}` - WebSocket for real-time collaboration

### 3. Version Control Service (:8083)
**Operations:**
- `POST /api/versions/{documentId}` - Create version snapshot
- `GET /api/versions/{documentId}` - Get version history
- `POST /api/versions/{documentId}/revert/{versionId}` - Revert to version
- `GET /api/versions/{documentId}/contributions` - User contributions
- `GET /api/versions/{documentId}/diff/{v1}/{v2}` - Compare versions
- `GET /api/versions/{versionId}` - Get specific version

### 4. API Gateway (:8080)
- Routes all requests to appropriate microservices
- Validates JWT tokens
- Handles CORS
- Logs requests/responses

## Database Schema

### Users Collection
```json
{
  "_id": "ObjectId",
  "username": "string",
  "email": "string (unique)",
  "password": "bcrypt_hash",
  "avatar": "url",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Documents Collection
```json
{
  "_id": "ObjectId",
  "title": "string",
  "content": "string",
  "ownerId": "ObjectId",
  "collaborators": [
    {"userId": "ObjectId", "permission": "edit|view"}
  ],
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Versions Collection
```json
{
  "_id": "ObjectId",
  "documentId": "ObjectId",
  "content": "string",
  "userId": "ObjectId",
  "timestamp": "timestamp",
  "description": "string",
  "changeStats": {
    "charsAdded": "number",
    "charsDeleted": "number"
  }
}
```

## Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd user-service
mvn test

# Generate coverage report
mvn test jacoco:report
```

## Architecture Decisions

### Real-time Collaboration
- Uses **simplified last-write-wins** strategy
- WebSocket connects directly to Document Service (bypasses Gateway for simplicity)
- Changes debounced (500ms) before broadcasting
- Future enhancement: Implement full Operational Transformation (OT) or CRDT

### Authentication
- JWT tokens with 24-hour expiration
- BCrypt password hashing (strength 12)
- Tokens validated by each service independently

### Version Control
- Auto-save every 5 minutes
- Stores full document snapshots (not deltas)
- Tracks per-user contributions

## Troubleshooting

See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues and solutions.

## Demo Scenarios

See [DEMO.md](DEMO.md) for step-by-step demonstration instructions.

## Project Structure

```
UniTask/
├── api-gateway/           # API Gateway service
├── user-service/          # User management microservice
├── document-service/      # Document editing microservice
├── version-service/       # Version control microservice
├── common-lib/            # Shared utilities, DTOs, exceptions
├── frontend/              # React application
├── pom.xml                # Parent Maven POM
├── start-backend.bat      # Windows script to start all backend services
├── start-frontend.bat     # Windows script to start frontend
└── README.md
```

## License

MIT License

## Authors

- Arhum Khan

## Acknowledgments

- Spring Boot framework
- React and Material-UI communities
- MongoDB
