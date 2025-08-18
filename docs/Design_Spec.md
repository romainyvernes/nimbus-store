# Design Specification

**Author:** Romain Yvernes  
**Date:** 07/31/2025

---

## 1️⃣ Project Overview

**Goal:**  
Build a fault-tolerant, horizontally scalable file storage system ("mini-S3") that stores files across multiple nodes, replicates data for reliability, and enables retrieval even during node failures.

**Motivation:**  
- Demonstrate distributed systems design skills for backend engineering roles.
- Showcase replication, failover, and scaling in a practical storage system.

---

## 2️⃣ System Requirements

### Functional Requirements
- ✅ Upload files via REST/gRPC API.
- ✅ Split large files into chunks and distribute across nodes.
- ✅ Replicate chunks to multiple nodes (default replication factor: 2 or 3).
- ✅ Retrieve full files even if a node is down.
- ✅ List stored files and metadata.

### Non-Functional Requirements
- **Reliability:** No data loss if a single node fails.
- **Scalability:** Add nodes and distribute load automatically.
- **Performance:** Support parallel uploads/downloads.
- **Observability:** Provide health checks and logging.

---

## 3️⃣ High-Level Architecture

![Architecture diagram.png](./Architecture%20diagram.png)

**Components:**
- **Client API:** Handles file uploads/downloads, communicates with Metadata Service.
- **Metadata Service:** Stores file info, chunk mapping, and node locations.
- **Storage Nodes:** Store file chunks, respond to store/retrieve requests.
- **Replication Manager:** Ensures chunks are replicated and rebalanced if a node fails.

**Communication:**  
- REST over HTTP (gRPC as future extension).
- Metadata Service coordinates nodes but does not store file data.

---

## 4️⃣ Data Model

**DTO Usage:**  
- All API endpoints must use Data Transfer Objects (DTOs) for both requests and responses, rather than exposing internal entity classes directly.
- Mapping between entities and DTOs should be handled in the service layer or via a dedicated mapper component to ensure separation of concerns and API stability.
- **Validation:** DTOs are validated using annotations (e.g., `@Valid`, `@NotNull`). Invalid requests will result in `400 Bad Request` responses.

**Database Schema (Metadata Service):**

| Table   | Fields                               | Description                         |
|---------|--------------------------------------|-------------------------------------|
| files   | id, filename, created_at             | File entries                        |
| chunks  | id, file_id, chunk_index, node_ids   | Mapping from chunks to nodes        |
| nodes   | id, address, status                  | Known storage nodes and health info |

---

## 5️⃣ Replication & Fault Tolerance

- **Replication Factor:** Default = 3 (configurable).
- **Write Path:** Client uploads chunk → Metadata assigns N nodes → Chunk sent in parallel.
- **Failure Detection:** Heartbeat messages from Metadata → Node every 5s.
- **Recovery:** If a node misses 3 heartbeats, it's marked as down. Metadata selects a healthy node to create a new replica and updates chunk mapping.
- **Chunk Replication Details:** Chunk upload returns metadata for all replicas. Replication is triggered and managed by the Replication Manager component.

---

## 6️⃣ API Endpoints (Phase 1 MVP)

**Error Handling:**  
- All endpoints return appropriate HTTP status codes for error cases (`404 Not Found`, `400 Bad Request`, `500 Internal Server Error`, etc.) to ensure RESTful consistency.

**API Consistency:**  
- All endpoints use DTOs for both requests and responses. Internal entities are never exposed directly.

### 6.1 Metadata Service

#### Files

- `POST /files/register`
  *Register a file and its metadata.*

  **Request:**  
  `multipart/form-data` with fields:
  - `file`: binary file
  - `chunkCount`: integer

  **Response (DTO):**
  ```json
  {
    "id": "uuid",
    "filename": "example.txt",
    "size": 12345,
    "chunkCount": 4,
    "status": "PENDING",
    "checksum": "..."
  }
  ```

- `GET /files/{id}`
  *Retrieve file metadata by UUID.*

  **Response (DTO):**
  ```json
  {
    "id": "uuid",
    "filename": "example.txt",
    "size": 12345,
    "chunkCount": 4,
    "status": "PENDING",
    "checksum": "..."
  }
  ```

- `PATCH /files/{id}/status`
  *Update file status.*

  **Request:**  
  Query parameter: `status` (e.g., `COMPLETED`)

  **Response:**  
  Returns only an appropriate HTTP status code (`200 OK` if updated, `400 Bad Request` if missing status, `404 Not Found` if file not found).

- `GET /files`
  *Retrieve metadata about all files (paginated).*

  **Request:**  
  Query parameters:
  - `page`: integer (default: 0)
  - `size`: integer (default: 10, maximum: 100)

  **Response:**
  ```json
  {
    "files": [
      {
        "id": "uuid",
        "filename": "example.txt",
        "size": 12345,
        "chunkCount": 4,
        "status": "PENDING",
        "checksum": "..."
      }
      // ...more file DTOs...
    ],
    "total": 42
  }
  ```

#### Chunks

- `POST /chunks`
  *Upload a chunk for a file.*

  **Request (JSON):**
  ```json
  {
    "checksum": "...",
    "chunkIndex": 0,
    "fileId": "uuid"
  }
  ```

  **Response (DTO):**
  ```json
  [
    {
      "id": "uuid",
      "chunkIndex": 0,
      "storageNodeId": "uuid",
      "checksum": "...",
      "fileId": "uuid",
      "status": "PENDING"
    }
    // ...other replicas...
  ]
  ```

- `GET /chunks/{chunkChecksum}`
  *Retrieve chunk metadata by checksum.*

  **Response (DTO):**
  ```json
  {
    "id": "uuid",
    "chunkIndex": 0,
    "storageNodeId": "uuid",
    "checksum": "...",
    "fileId": "uuid",
    "status": "PENDING"
  }
  ```

- `PATCH /chunks/{id}/status`
  *Update chunk status.*

  **Request:**  
  Query parameter: `status` (e.g., `COMPLETED`)

  **Response:**  
  Returns only an appropriate HTTP status code (`200 OK` if updated, `400 Bad Request` if missing status).

#### Storage Nodes

- `POST /nodes/register`
  *Register a new storage node with a provided UUID and URL.*

  **Request:**
  ```json
  {
    "id": "uuid",
    "url": "http://node-url"
  }
  ```

  **Response:**  
  Returns only an appropriate HTTP status code (`200 OK`).

- `POST /nodes/heartbeat`
  *Update last heartbeat for a node.*

  **Request:**
  ```json
  {
    "id": "uuid"
  }
  ```

  **Response:**  
  Returns only an appropriate HTTP status code (`200 OK` if updated, `404 Not Found` if node not found).

#### Health Check

- `GET /health`
  *Service health check.*

  **Response:**  
  Plain text: `"Metadata Service is running!"`

---

### 6.2 Storage Node API

#### Health Check

- `GET /health`
  *Service health check.*

  **Response:**  
  Plain text: `"Storage Node API is running!"`

#### Chunks

- `POST /chunks/upload`
  *Upload a chunk to the storage node.*

  **Request:**  
  - Request body: binary chunk data

  **Response:**  
  Returns `201 Created` and the checksum as body if successful.

- `GET /chunks/{chunkChecksum}`
  *Retrieve a chunk from the storage node by checksum.*

  **Response:**  
  - Response body: binary chunk data
  - Returns `200 OK` if found, `404 Not Found` if not.

---

### 6.3 Client API

- `POST /files`
  *Upload a file.*

- `GET /files/{fileId}`
  *Download a file.*

---

## 7️⃣ Scaling Strategy

- Use round-robin or hash-based placement to assign chunks evenly.
- Future: Consistent hashing for dynamic cluster membership.
- Auto-scaling: Add nodes and update metadata registry dynamically.

---

## 8️⃣ Trade-Offs and Design Choices

- **DTOs:** All API endpoints use DTOs for input/output. Entities are mapped to DTOs in the service layer or with a dedicated mapper.
- **Validation:** DTOs are validated and invalid requests result in `400 Bad Request`.
- **Consistency:** Eventual consistency for simplicity and speed.
- **API:** REST initially for rapid development; gRPC for future efficiency.
- **Tech Stack:** Java + Spring Boot for fast iteration and industry alignment.
- **Metadata Service:** Centralized for simplicity; future: leader election (Raft/Zookeeper) for high availability.

---

## 9️⃣ Testing Strategy

- **Unit Tests:** Isolate and test individual components and methods using mocks.
- **Integration Tests:** Use `@SpringBootTest` to verify end-to-end behavior, including file system and service interactions.
- **Error Handling:** Tests verify correct HTTP status codes for all error scenarios.

---

## 🔟 Future Enhancements (Phases 2–4)

- Leader election for Metadata Service.
- Erasure coding for storage efficiency.
- JWT-based authentication and secure file sharing.
- Cloud deployment (Kubernetes + AWS S3/EBS).
- Performance benchmarking and reporting.
- Ongoing improvements to validation and error handling.
