# minecraft-workshop-server

Cloud AI Builder microservice for **Minecraft Workshop** and **WorldXR**.

Runs on Google Cloud Run (Java 21). Interacts with Google Gemini for architectural reasoning, translates natural language prompts into voxel structures, and caches builds via semantic vector embeddings (`text-embedding-004`).

---

## Architecture Overview

```
 [WorldXR Handheld / Headset]
              │
       HTTPS / REST (POST /api/v1/build)
              ▼
  ┌─────────────────────────────────────────────────────────┐
  │         minecraft-workshop-server (Cloud Run)           │
  │                                                         │
  │  1. Semantic Cache (text-embedding-004, cosine > 0.88)  │
  │     └─► Hit (< 50ms): Return cached voxel palette       │
  │  2. BuilderEngine Integration                           │
  │     ├─► SampleBuilderEngine (Scaffolded baseline)       │
  │     └─► Grammar Compiler (Connecting to Workshop)       │
  │  3. Gemini 2.5 Flash Inference                          │
  │     └─► Prompt -> Architectural Spec                    │
  └─────────────────────────────────────────────────────────┘
```

---

## API Endpoints

### Live Production Base URL
```
https://minecraft-workshop-server-481791752877.us-west2.run.app
```

### 1. Health Check
```http
GET /health
```
**Response:**
```json
{
  "status": "ok",
  "service": "minecraft-workshop-server",
  "version": "1.0.0",
  "uptimeMs": 4210
}
```

### 2. Build Structure
```http
POST /api/v1/build
Content-Type: application/json

{
  "prompt": "a medieval stone watchtower",
  "style": "medieval",
  "seed": 42
}
```
**Response:**
```json
{
  "structureId": "8f1a2b3c-...",
  "title": "Medieval Stone Watchtower",
  "story": "A sturdy medieval guard outpost with oak framing, stone brick curtain walls, and crenelated parapet.",
  "width": 16,
  "height": 16,
  "depth": 16,
  "palette": ["air", "cobblestone", "oak_log", "stone_bricks", "glass", "oak_planks"],
  "voxelDataB64": "...",
  "cached": true,
  "similarityScore": 0.96,
  "generationTimeMs": 14
}
```

### 3. Favorites Catalog
```http
GET /api/v1/favorites
```

---

## Building & Running Locally

### Prerequisites
- JDK 21+

### Build
```bash
./mvnw clean package
```

### Run
```bash
java -jar target/minecraft-workshop-server-1.0.0-SNAPSHOT.jar
```
Server listens on `http://localhost:8080`.

---

## Deploying to Google Cloud Run

```bash
gcloud run deploy minecraft-workshop-server \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars GEMINI_API_KEY=YOUR_KEY
```
