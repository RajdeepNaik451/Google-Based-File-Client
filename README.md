# GFS Client

## Overview
The GFS Client is responsible for interacting with the Google File System (GFS)
implementation. It communicates with the **Master** to obtain metadata and
directly communicates with **ChunkServers** to perform data read and write
operations.

The client does not store metadata or data locally. Instead, it coordinates
all operations by following the GFS architecture principles.

---

## Responsibilities
- Request file metadata from the Master
- Create new files
- Retrieve chunk locations
- Write data to ChunkServers
- Read data from ChunkServers
- Perform append operations

---

## Architecture Role
The client follows a **two-step communication model**:

1. **Metadata Operations**
   - Communicates with the Master
   - Operations: CREATE_FILE, GET_CHUNKS, APPEND

2. **Data Operations**
   - Communicates directly with ChunkServers
   - Operations: WRITE_CHUNK, READ_CHUNK

This design prevents the Master from becoming a bottleneck.

---

## Communication Protocol
All communication uses:
- Java Sockets
- ObjectInputStream / ObjectOutputStream
- Serialized `Message` objects

A lightweight RPC helper class (`RPC.java`) is used to manage socket
communication safely and consistently.

---

## How to Run

### Prerequisites
- Master server must be running
- At least one ChunkServer must be registered

### Steps
1. Start the Master
2. Start one or more ChunkServers
3. Run the client:

```bash
java GFSClient
