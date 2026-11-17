# minecraft-workshop-server

Cloud AI Builder microservice and real-time WebSocket broadcast relay connecting Minecraft Paper servers (`minecraft-workshop`) to Android XR clients (`WorldXR`). Runs on Google Cloud Run (Java 21).

## Commands
- Build & test: `.\mvnw.cmd test -q`
- Package fat jar: `.\mvnw.cmd clean package -DskipTests -q`
- Run locally: `.\mvnw.cmd spring-boot:run`
- Health check: `curl -s https://minecraft-workshop-server-481791752877.us-west2.run.app/health`

## Architecture & Endpoints
- `GET /health`: Service health check (status, uptime, version).
- `POST /api/v1/build`: Prompt -> paletted 16x16x16 voxel volume via semantic embedding cache and Gemini.
- `GET /api/v1/favorites`: Curated architectural catalog (watchtower, cottage, fountain, gatehouse).
- `WS /ws/stream/{sessionId}`: WebSocket relay:
  - `?role=broadcaster`: Minecraft Paper server broadcasting world telemetry.
  - `?role=viewer`: WorldXR client receiving snapshots, 10 Hz positions, deltas, and mob sync.

## Design Rules
1. **Low Latency Forwarding**: WebSocket messages are routed directly to viewer sessions without blocking or synchronous I/O.
2. **Boundary Validation**: Request payloads are sanitized and validated at the controller boundary.
3. **Graceful Session Cleanup**: Abandoned broadcaster and viewer channels are pruned automatically to prevent memory leaks in Cloud Run instances.
4. **Secrets Discipline**: `GEMINI_API_KEY` is loaded strictly from environment variables. Never print, log, or commit secret keys.

## Definition of Done
1. Maven unit tests pass (`.\mvnw.cmd test`).
2. Cloud Run deployment responds with `{"status":"ok"}` on `/health`.
3. WebSocket relay verifies bidirectional transmission between Minecraft `/workshop xr follow` and WorldXR viewer.
4. Git commit includes tests, references tracking issue, and ends with co-author attribution.
