# Document Editing Service

Microservice for document creation, editing, sharing, and real-time collaboration via WebSocket.

## Port

**8082**

## REST API Endpoints

### Document Management

#### Create Document
```http
POST /api/documents
Content-Type: application/json

{
  "title": "My Document",
  "content": "Initial content",
  "ownerId": "507f1f77bcf86cd799439011"
}
```

#### Get Document
```http
GET /api/documents/{documentId}?userId={userId}
```

#### Update Document
```http
PUT /api/documents/{documentId}?userId={userId}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content",
  "lastModifiedBy": "507f1f77bcf86cd799439011"
}
```

#### Share Document
```http
POST /api/documents/{documentId}/share?ownerId={ownerId}
Content-Type: application/json

{
  "userId": "507f1f77bcf86cd799439012",
  "permission": "edit"
}
```

#### Delete Document
```http
DELETE /api/documents/{documentId}?userId={userId}
```

#### Get All Accessible Documents
```http
GET /api/documents?userId={userId}
```

## WebSocket API

### Connection
```javascript
const socket = new SockJS('http://localhost:8082/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  console.log('Connected to WebSocket');
});
```

### Subscribe to Document Updates
```javascript
stompClient.subscribe('/topic/document/' + documentId, (message) => {
  const data = JSON.parse(message.body);
  handleDocumentUpdate(data);
});
```

### Send Document Changes
```javascript
stompClient.send('/app/document/' + documentId + '/edit', {}, JSON.stringify({
  type: 'content-change',
  documentId: documentId,
  userId: userId,
  username: username,
  content: updatedContent,
  timestamp: new Date().toISOString()
}));
```

### Join Document Editing Session
```javascript
stompClient.send('/app/document/' + documentId + '/join', {}, JSON.stringify({
  documentId: documentId,
  userId: userId,
  username: username
}));
```

### Leave Document Editing Session
```javascript
stompClient.send('/app/document/' + documentId + '/leave', {}, JSON.stringify({
  documentId: documentId,
  userId: userId,
  username: username
}));
```

## Message Types

### Content Change
```json
{
  "type": "content-change",
  "documentId": "507f1f77bcf86cd799439011",
  "userId": "507f1f77bcf86cd799439012",
  "username": "john_doe",
  "content": "Updated document content",
  "timestamp": "2024-01-15T10:30:00"
}
```

### User Joined
```json
{
  "type": "user-joined",
  "documentId": "507f1f77bcf86cd799439011",
  "userId": "507f1f77bcf86cd799439012",
  "username": "john_doe",
  "timestamp": "2024-01-15T10:30:00"
}
```

### User Left
```json
{
  "type": "user-left",
  "documentId": "507f1f77bcf86cd799439011",
  "userId": "507f1f77bcf86cd799439012",
  "username": "john_doe",
  "timestamp": "2024-01-15T10:30:00"
}
```

## API Documentation

Swagger UI: http://localhost:8082/swagger-ui.html

OpenAPI JSON: http://localhost:8082/api-docs

## Running the Service

```bash
# From document-service directory
mvn spring-boot:run

# Or from parent directory
cd document-service && mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

## Health Check

```http
GET http://localhost:8082/actuator/health
```

## Configuration

- MongoDB: localhost:27017/collab_edit_db
- WebSocket Endpoint: /ws
- STOMP Application Prefix: /app
- STOMP Broker Prefix: /topic
- JWT Secret: Must match other services

## Real-time Collaboration Flow

1. **Client connects** to WebSocket endpoint `/ws`
2. **Client subscribes** to `/topic/document/{documentId}`
3. **Client sends join message** to `/app/document/{documentId}/join`
4. **Client edits** and sends changes to `/app/document/{documentId}/edit`
5. **Server broadcasts** changes to all subscribers
6. **Other clients receive** and apply changes
7. **Client sends leave message** before disconnecting

## Permission Model

- **Owner**: Full control (read, write, share, delete)
- **Collaborator with "edit"**: Can read and write
- **Collaborator with "view"**: Can only read

## Dependencies

- Spring Boot Web
- Spring Boot WebSocket
- Spring Data MongoDB
- Spring Security
- Springdoc OpenAPI (Swagger)
- Lombok
- Common Library
