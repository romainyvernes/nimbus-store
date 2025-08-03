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

---

## 6️⃣ API Endpoints (Phase 1 MVP)

- `POST /files/upload`  
  *Upload a file.*

  **Request (multipart/form-data):**
  ```json
  {
    "file": "<binary>"
  }
  ```

  **Response:**
  ```json
  {
    "fileId": "abc123",
    "status": "uploaded"
  }
  ```

- `GET /files/download/{fileId}`  
  *Download a file.*

  **Response:**  
  Binary file stream.

- `GET /files`  
  *List all files.*

  **Response:**
  ```json
  [
    {
      "fileId": "abc123",
      "filename": "example.txt",
      "createdAt": "2025-07-31T12:00:00Z"
    }
    // ...more files...
  ]
  ```

- `GET /health`  
  *Node/cluster status.*

  **Response:**
  ```json
  {
    "status": "healthy",
    "nodes": [
      {"id": "node1", "status": "up"},
      {"id": "node2", "status": "up"}
    ]
  }
  ```

---

## 7️⃣ Scaling Strategy

- Use round-robin or hash-based placement to assign chunks evenly.
- Future: Consistent hashing for dynamic cluster membership.
- Auto-scaling: Add nodes and update metadata registry dynamically.

---

## 8️⃣ Trade-Offs and Design Choices

- **Consistency:** Eventual consistency for simplicity and speed.
- **API:** REST initially for rapid development; gRPC for future efficiency.
- **Tech Stack:** Java + Spring Boot for fast iteration and industry alignment.
- **Metadata Service:** Centralized for simplicity; future: leader election (Raft/Zookeeper) for high availability.

---

## 9️⃣ Future Enhancements (Phases 2–4)

- Leader election for Metadata Service.
- Erasure coding for storage efficiency.
- JWT-based authentication and secure file sharing.
- Cloud deployment (Kubernetes + AWS S3/EBS).
- Performance benchmarking and reporting.
