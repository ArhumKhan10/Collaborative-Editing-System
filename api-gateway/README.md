# API Gateway

Central routing gateway for all microservices requests.

## Port

**8080**

## Routes

All requests to microservices go through the API Gateway:

### User Service
```
http://localhost:8080/api/users/** → http://localhost:8081/api/users/**
```

### Document Service
```
http://localhost:8080/api/documents/** → http://localhost:8082/api/documents/**
```

### Version Service
```
http://localhost:8080/api/versions/** → http://localhost:8083/api/versions/**
```

## Features

### Request Routing
- Automatically routes requests to appropriate microservice
- Path-based routing
- Load balancing ready (for multiple instances)

### CORS Configuration
- Global CORS enabled for frontend origins
- Allows credentials (JWT tokens)
- Supports all common HTTP methods

### Request Logging
- Logs all incoming requests
- Logs response status and duration
- Helps with debugging and monitoring

### Health Check
```http
GET http://localhost:8080/actuator/health
```

### Gateway Routes Info
```http
GET http://localhost:8080/actuator/gateway/routes
```

## WebSocket Note

**Important:** WebSocket connections for real-time collaboration bypass the API Gateway and connect directly to Document Service on port 8082.

**Why?** Spring Cloud Gateway WebSocket proxying is complex to configure. Direct connection is simpler and works reliably for the demo.

**Connection:**
```javascript
// WebSocket connects directly to Document Service
const socket = new SockJS('http://localhost:8082/ws');
```

## Running the Gateway

```bash
# From api-gateway directory
mvn spring-boot:run

# Or from parent directory
cd api-gateway && mvn spring-boot:run
```

**Important:** Start API Gateway BEFORE other services for proper routing.

## Configuration

- Port: 8080
- Routes configured in application.yml
- JWT Secret: Must match other services
- CORS: Allows localhost:3000 and localhost:5173

## Example Request Flow

1. **Frontend** → `POST http://localhost:8080/api/users/login`
2. **API Gateway** → Logs request, routes to User Service
3. **User Service** (8081) → Processes login
4. **API Gateway** → Logs response, returns to frontend
5. **Frontend** → Receives JWT token

## Monitoring

View all configured routes:
```bash
curl http://localhost:8080/actuator/gateway/routes
```

Check gateway health:
```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Service Not Reachable
- Ensure target microservice is running
- Check port configuration in application.yml
- Verify network connectivity

### CORS Errors
- Check allowed origins in application.yml
- Ensure frontend URL matches configured origin
- Verify credentials are allowed

### Route Not Found (404)
- Check route predicates in application.yml
- Ensure path matches configured pattern
- View routes at `/actuator/gateway/routes`

## Future Enhancements

- JWT validation at gateway level
- Rate limiting
- Circuit breaker patterns
- Service discovery (Eureka)
- WebSocket proxy configuration
- Request/response transformation
