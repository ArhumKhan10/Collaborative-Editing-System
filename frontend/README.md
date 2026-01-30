# Collaborative Editing Frontend

React-based frontend application for the collaborative editing system.

## Technology Stack

- **React 18** with Hooks
- **Vite** - Fast build tool
- **Material-UI (MUI)** - UI components
- **React Router** - Navigation
- **Axios** - HTTP requests
- **SockJS + STOMP** - WebSocket for real-time collaboration
- **React-Quill** - Rich text editor
- **React-Toastify** - Notifications

## Installation

```bash
npm install
```

## Running the Application

```bash
# Development mode (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Features

### Authentication
- User registration
- User login with JWT token
- Persistent authentication (localStorage)
- Auto-redirect on auth state changes

### Document Management
- Create new documents
- View all accessible documents (owned + shared)
- Delete documents (owner only)
- Document metadata (title, last modified)

### Real-time Collaboration
- WebSocket connection to Document Service
- Live content synchronization across multiple users
- Active users indicator
- User join/leave notifications
- Debounced updates (500ms) to reduce network traffic

### Document Editing
- Rich text editor (Quill.js)
- Formatting: headers, bold, italic, underline, lists
- Auto-save functionality
- Manual save button
- Title editing

### Version Control
- Create manual version snapshots
- View version history
- Revert to previous versions
- See change statistics (chars added/deleted)

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar.jsx           # Top navigation bar
│   │   └── PrivateRoute.jsx     # Protected route wrapper
│   ├── context/
│   │   └── AuthContext.jsx      # Authentication state management
│   ├── hooks/
│   │   └── useWebSocket.js      # WebSocket connection hook
│   ├── pages/
│   │   ├── Login.jsx            # Login page
│   │   ├── Register.jsx         # Registration page
│   │   ├── Dashboard.jsx        # Document list
│   │   └── DocumentEditor.jsx   # Collaborative editor
│   ├── services/
│   │   ├── api.js               # Axios instance with interceptors
│   │   ├── authService.js       # User authentication APIs
│   │   ├── documentService.js   # Document CRUD APIs
│   │   └── versionService.js    # Version control APIs
│   ├── App.jsx                  # Main app with routes
│   ├── main.jsx                 # Entry point
│   └── index.css                # Global styles
├── package.json
├── vite.config.js
└── index.html
```

## API Endpoints

### User Service (via API Gateway :8080)
- POST `/api/users/register` - Register new user
- POST `/api/users/login` - Login user (returns JWT token)
- GET `/api/users/profile/:userId` - Get user profile

### Document Service (via API Gateway :8080)
- POST `/api/documents` - Create document
- GET `/api/documents/:id?userId=` - Get document
- PUT `/api/documents/:id?userId=` - Update document
- DELETE `/api/documents/:id?userId=` - Delete document
- GET `/api/documents?userId=` - Get all accessible documents
- POST `/api/documents/:id/share` - Share document with user

### Version Service (via API Gateway :8080)
- POST `/api/versions/:documentId` - Create version
- GET `/api/versions/:documentId` - Get version history
- POST `/api/versions/:documentId/revert/:versionId` - Revert to version

### WebSocket (Direct to Document Service :8082)
- **Connect:** `ws://localhost:8082/ws`
- **Subscribe:** `/topic/document/:documentId`
- **Send edit:** `/app/document/:documentId/edit`
- **Send join:** `/app/document/:documentId/join`
- **Send leave:** `/app/document/:documentId/leave`

## Environment Configuration

The frontend connects to:
- **API Gateway:** http://localhost:8080
- **WebSocket:** http://localhost:8082/ws (direct to Document Service)

These are configured in:
- `src/services/api.js` - HTTP API calls
- `src/hooks/useWebSocket.js` - WebSocket connection

## Authentication Flow

1. User enters credentials in Login page
2. Frontend sends POST to `/api/users/login`
3. Backend validates and returns JWT token + user info
4. Frontend stores token in localStorage
5. Axios interceptor adds token to all subsequent requests
6. Protected routes check authentication status

## Real-time Collaboration Flow

1. User opens document
2. Frontend connects to WebSocket at `/ws`
3. Subscribes to `/topic/document/:documentId`
4. Sends join message to `/app/document/:documentId/join`
5. On content change (debounced 500ms):
   - Sends update to `/app/document/:documentId/edit`
   - Server broadcasts to all subscribers
   - Other clients receive and apply changes
6. On close, sends leave message

## Test Accounts

Use these credentials for testing:
- **Alice:** alice@test.com / password123
- **Bob:** bob@test.com / password123

## Building for Production

```bash
# Build optimized production bundle
npm run build

# Output will be in dist/ folder
# Deploy dist/ folder to web server
```

## Troubleshooting

### WebSocket Connection Failed
- Ensure Document Service is running on port 8082
- Check browser console for connection errors
- Verify CORS is enabled in Document Service

### API Calls Failing
- Ensure API Gateway is running on port 8080
- Check if backend services are up and healthy
- Verify JWT token is not expired (24 hours)

### Real-time Updates Not Working
- Check WebSocket connection status (green dot)
- Verify multiple users are on the same document
- Check browser console for WebSocket messages
- Ensure debounce delay (500ms) has passed

## Development Tips

- Hot reload is enabled - changes reflect immediately
- Check browser DevTools > Network > WS for WebSocket messages
- Use React DevTools browser extension for component debugging
- Toast notifications show all important events
