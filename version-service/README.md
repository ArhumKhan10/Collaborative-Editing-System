# Version Control Service

Microservice for document version history, revert operations, and user contributions tracking.

## Port

**8083**

## Endpoints

### Version Management

#### Create Version Snapshot
```http
POST /api/versions/{documentId}
Content-Type: application/json

{
  "content": "Document content snapshot",
  "userId": "507f1f77bcf86cd799439011",
  "description": "Before major refactoring"
}
```

#### Get Version History
```http
GET /api/versions/{documentId}
```

Response: Array of versions sorted by timestamp (newest first)

#### Get Specific Version
```http
GET /api/versions/version/{versionId}
```

#### Revert to Previous Version
```http
POST /api/versions/{documentId}/revert/{versionId}?userId={userId}
```

Creates a new version with the content from the specified version.

#### Get User Contributions
```http
GET /api/versions/{documentId}/contributions
```

Response:
```json
[
  {
    "userId": "507f1f77bcf86cd799439011",
    "username": "john_doe",
    "stats": {
      "editsCount": 15,
      "charsAdded": 1250,
      "charsDeleted": 320,
      "versionsCreated": 5
    }
  }
]
```

## Features

### Automatic Version Creation
- Versions can be created manually via API
- Change statistics calculated automatically (chars added/deleted)

### Version History
- Versions stored with full content snapshots
- Sorted by timestamp (newest first)
- Includes user who created each version
- Optional description for each version

### Revert Functionality
- Restore document to any previous version
- Creates new version entry (doesn't delete history)
- Preserves full version lineage

### Contribution Tracking
- Tracks per-user statistics:
  - Total edits count
  - Characters added
  - Characters deleted
  - Versions created
- Aggregated across all versions for a document

## API Documentation

Swagger UI: http://localhost:8083/swagger-ui.html

OpenAPI JSON: http://localhost:8083/api-docs

## Running the Service

```bash
# From version-service directory
mvn spring-boot:run

# Or from parent directory
cd version-service && mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

## Health Check

```http
GET http://localhost:8083/actuator/health
```

## Configuration

- MongoDB: localhost:27017/collab_edit_db
- JWT Secret: Must match other services
- Collections: `versions`, `contributions`

## Dependencies

- Spring Boot Web
- Spring Data MongoDB
- Spring Security
- Springdoc OpenAPI (Swagger)
- Lombok
- Common Library

## Use Cases

1. **Manual Checkpoint**: User explicitly saves a version before making major changes
2. **Collaboration History**: Track who contributed what to the document
3. **Undo/Revert**: Restore document to previous state after unwanted changes
4. **Audit Trail**: Maintain complete history of document evolution
