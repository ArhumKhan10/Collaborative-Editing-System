# Demo Guide - Collaborative Editing System

Complete step-by-step guide for demonstrating the system.

## Pre-Demo Checklist

### 1. Ensure MongoDB is Running
```bash
# Check MongoDB Compass
# Verify "MyConnection" is connected to localhost:27017
```

### 2. Build All Services
```bash
# In UniTask directory
mvn clean install
```

### 3. Start All Services (in order)
```bash
# Method 1: Use the start script
start-backend.bat

# Method 2: Manual start (in separate terminals)
# Terminal 1: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 2: User Service
cd user-service && mvn spring-boot:run

# Terminal 3: Document Service
cd document-service && mvn spring-boot:run

# Terminal 4: Version Service
cd version-service && mvn spring-boot:run
```

### 4. Wait for Services to Start (30-60 seconds)
Check health endpoints:
- http://localhost:8080/actuator/health (API Gateway)
- http://localhost:8081/actuator/health (User Service)
- http://localhost:8082/actuator/health (Document Service)
- http://localhost:8083/actuator/health (Version Service)

### 5. Start Frontend
```bash
# Terminal 5: React Frontend
cd frontend
npm install  # First time only
npm run dev
```

Access at: http://localhost:3000

## Demo Script (20 minutes)

### Part 1: Architecture Overview (3 minutes)

**Show:** Project structure in IDE

**Explain:**
- 4 microservices: API Gateway, User, Document, Version
- Common library for shared code
- React frontend
- MongoDB for persistence
- WebSocket for real-time collaboration

**Show:** Architecture diagram in README.md

**Key Points:**
- Microservices communicate via REST APIs
- API Gateway routes all requests
- WebSocket connects directly to Document Service (explain why)
- JWT tokens for authentication across all services

---

### Part 2: User Management Demo (3 minutes)

**Scenario:** Register and login a new user

1. **Show Registration:**
   - Open http://localhost:3000
   - Click "Sign Up"
   - Register: 
     - Username: demo_user
     - Email: demo@test.com
     - Password: password123
   - Show success message and redirect to login

2. **Show Login:**
   - Login with: demo@test.com / password123
   - Show JWT token in browser DevTools:
     - Open DevTools (F12)
     - Application tab → Local Storage → http://localhost:3000
     - Show `token` and `user` stored

3. **Show Swagger UI:**
   - Open http://localhost:8081/swagger-ui.html
   - Show all User Service endpoints
   - Explain RESTful API design

4. **Show MongoDB:**
   - Open MongoDB Compass
   - Show `collab_edit_db` database
   - Show `users` collection
   - Find the newly created user

**Key Points:**
- BCrypt password hashing (password not stored in plain text)
- JWT token with 24-hour expiration
- RESTful API design

---

### Part 3: Document Creation & Editing (2 minutes)

**Scenario:** Create and edit a document

1. **Create Document:**
   - Click "New Document" button
   - Enter title: "Project Proposal"
   - Click Create
   - Show redirect to editor

2. **Edit Document:**
   - Type some content in the rich text editor
   - Use formatting: bold, italic, headers, lists
   - Click "Save"
   - Show success notification

3. **Show API Calls:**
   - Open DevTools → Network tab
   - Show REST API calls to backend
   - Show request/response headers (Authorization: Bearer ...)

4. **Show MongoDB:**
   - Refresh MongoDB Compass
   - Show `documents` collection
   - Show the created document

**Key Points:**
- Rich text editing with Quill.js
- Auto-save functionality
- Document ownership and permissions

---

### Part 4: Real-time Collaboration (5 minutes) ⭐ **MOST IMPORTANT**

**Scenario:** Two users editing the same document simultaneously

1. **Setup:**
   - Browser 1 (Chrome): Already logged in as demo@test.com
   - Browser 2 (Firefox or Chrome Incognito): Login as alice@test.com / password123

2. **Share Document (Browser 1):**
   - Note the document ID from URL (e.g., /document/507f1f77bcf86cd799439011)
   - Go to Dashboard
   - Share document with Alice (if share feature implemented)
   - OR: Manually open same document in Browser 2

3. **Demonstrate Real-time Sync:**
   - **Browser 1:** Type "Hello from Demo User"
   - **Browser 2:** See text appear in real-time (500ms delay)
   - **Browser 2:** Type "Hello back from Alice"
   - **Browser 1:** See Alice's text appear

4. **Show WebSocket Connection:**
   - **Browser 1 DevTools:** Network → WS (WebSocket)
   - Show WebSocket connection to ws://localhost:8082/ws
   - Show messages being sent/received
   - Show message types: content-change, user-joined, user-left

5. **Show Active Users:**
   - Point to "2 online" chip at top of editor
   - Show "Active users: demo_user, alice" at bottom

6. **Terminal Logs:**
   - Show Document Service terminal
   - Point out WebSocket connection logs
   - Show message broadcast logs

**Key Points:**
- WebSocket for real-time bidirectional communication
- SockJS + STOMP protocol
- Debouncing (500ms) to reduce network traffic
- Last-write-wins conflict resolution (simplified OT)
- Server broadcasts changes to all connected clients

---

### Part 5: Version Control (3 minutes)

**Scenario:** Create versions and revert

1. **Create Manual Version:**
   - In document editor, click "Save Version"
   - Enter description: "Initial draft"
   - Show success message

2. **Make Changes:**
   - Edit the document content significantly
   - Save again
   - Create another version: "Added introduction"

3. **View Version History:**
   - Click "Versions" button
   - Show version history drawer
   - Show timestamps and descriptions
   - Show change statistics (+X/-Y characters)

4. **Revert to Previous Version:**
   - Click on earlier version
   - Confirm revert
   - Show content restored to previous state
   - Show new version created (revert doesn't delete history)

5. **Show MongoDB:**
   - Open `versions` collection
   - Show multiple version documents
   - Show content snapshots

**Key Points:**
- Full content snapshots (not deltas)
- Version history preserved
- Revert creates new version
- Change statistics tracked

---

### Part 6: API Gateway & Microservices Communication (2 minutes)

**Show Terminal Logs:**

1. **API Gateway Logs:**
   ```
   [API-GATEWAY] - Incoming request: POST /api/documents
   [API-GATEWAY] - Response: 201 - Duration: 45ms
   ```

2. **Service Logs:**
   - Show User Service logging authentication
   - Show Document Service logging CRUD operations
   - Show Version Service logging version creation

3. **Show Gateway Routes:**
   - Open http://localhost:8080/actuator/gateway/routes
   - Show JSON response with all configured routes

4. **Explain Request Flow:**
   ```
   React Frontend (http://localhost:3000)
         ↓
   API Gateway (http://localhost:8080)
         ↓ (routes based on /api/...)
   ┌─────┴─────┬──────────┬──────────┐
   │           │          │          │
   User     Document  Version    MongoDB
   (8081)   (8082)    (8083)    (27017)
   ```

**Key Points:**
- Single entry point (API Gateway)
- Path-based routing
- Request/response logging
- Each service is independent
- Can scale services independently

---

### Part 7: Testing Demo (2 minutes)

**Show JUnit Tests:**

1. **Run Tests:**
   ```bash
   # In user-service directory
   cd user-service
   mvn test
   ```

2. **Show Test Output:**
   - Show number of tests run
   - Show test coverage
   - Highlight key tests:
     - User registration (success, duplicate)
     - Login (success, wrong password)
     - JWT validation

3. **Show Test Code:**
   - Open `UserServiceTest.java`
   - Show unit test with Mockito
   - Explain test structure (Arrange-Act-Assert)

**Key Points:**
- Unit tests with JUnit 5 + Mockito
- Integration tests with MockMvc
- Test coverage for critical paths
- Automated testing ensures reliability

---

## Q&A Preparation

### Likely Questions & Answers

**Q: Why doesn't WebSocket go through the API Gateway?**

**A:** Spring Cloud Gateway's WebSocket proxying is complex to configure with STOMP protocol. Direct connection is simpler and more reliable for the demo. In production, we'd either:
- Configure reactive WebSocket proxy in Gateway
- Use a dedicated WebSocket gateway (e.g., NGINX)
- Keep direct connection if acceptable

---

**Q: How do you handle concurrent edit conflicts?**

**A:** We use a simplified "last-write-wins" strategy:
- Changes are debounced (500ms) before sending
- Server broadcasts latest content to all clients
- Clients apply changes if not currently typing
- For production, we'd implement Operational Transformation (OT) or CRDT libraries like Yjs

---

**Q: How do you ensure security across microservices?**

**A:** 
- JWT tokens for authentication (24-hour expiration)
- BCrypt password hashing (strength 12)
- Each service validates JWT independently (shared secret)
- API Gateway can validate tokens before routing (future enhancement)
- HTTPS in production

---

**Q: How would you scale this system?**

**A:**
- Deploy each microservice in containers (Docker)
- Use Kubernetes for orchestration
- Add service discovery (Eureka, Consul)
- Implement caching (Redis)
- Database replication for MongoDB
- Load balancer in front of API Gateway
- Horizontal scaling of each service independently

---

**Q: What about database consistency across services?**

**A:**
- Each service has its own MongoDB collections
- Eventually consistent model
- For stronger consistency, implement:
  - Saga pattern for distributed transactions
  - Event sourcing with message queue (Kafka, RabbitMQ)

---

**Q: Why MongoDB instead of PostgreSQL?**

**A:**
- Document-oriented data model fits well (documents, versions)
- Flexible schema (can add fields without migration)
- Good for rapid development
- Horizontal scalability
- For production, choice depends on requirements

---

## Demo Backup Plan

If something fails during live demo:

### 1. Service Won't Start
- Show pre-recorded video of running system
- Show pre-taken screenshots
- Walk through code instead

### 2. WebSocket Connection Fails
- Fallback to showing HTTP API calls only
- Show WebSocket code and explain how it would work
- Show pre-recorded video of real-time collaboration

### 3. Database Issues
- Show MongoDB Compass with pre-populated data
- Explain schema and show example documents

### 4. Network/Browser Issues
- Have backup laptop ready
- Have screenshots prepared
- Have architecture diagrams printed

## Post-Demo Checklist

1. **Stop All Services:**
   ```bash
   stop-all.bat
   ```

2. **Check Ports:**
   ```bash
   netstat -ano | findstr "8080 8081 8082 8083 3000"
   ```

3. **Review Questions:**
   - Note questions you couldn't answer
   - Research for follow-up

## Time Management

- **Total Time: 20 minutes**
- Architecture: 3 min
- User Management: 3 min
- Document Editing: 2 min
- **Real-time Collaboration: 5 min** (most important!)
- Version Control: 3 min
- Microservices: 2 min
- Testing: 2 min

**Buffer: 5-10 min for Q&A**
