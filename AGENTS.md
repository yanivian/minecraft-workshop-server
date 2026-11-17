# Working on minecraft-workshop-server (for any coding agent)

Cloud AI Builder microservice and real-time WebSocket broadcast relay for **Minecraft Workshop** and **WorldXR**. Runs on Google Cloud Run (Java 21). The rules in [CLAUDE.md](CLAUDE.md) apply to every agent: read it first.

## The lifecycle, step by step

1. **Issue first.** Every change has an issue or cross-repo feature tracking (e.g. `minecraft-workshop#287`). Bug fixes start with a failing unit test reproducing the input.
2. **Branch** from `origin/main` (`git fetch; git checkout -B <branch> origin/main`). Never rewrite `main`.
3. **Build and unit tests** (Java 21, Maven wrapper):
   ```powershell
   .\mvnw.cmd test -q
   ```
   Package executable jar:
   ```powershell
   .\mvnw.cmd clean package -DskipTests -q
   ```
4. **Architecture & Components**:
   - `stream.SessionRelayService`: Low-latency WebSocket router (`/ws/stream/{sessionId}`). Matches broadcasters (Minecraft Paper server via `/workshop xr follow`) with viewers (`WorldXR` clients). Broadcasts baseline snapshots, high-frequency player coordinates (10 Hz), block deltas, celestial environment telemetry, and mob entity synchronization. Automatically prunes empty sessions after inactivity.
   - `ai.GeminiClient`: Gemini 2.5 Flash architectural synthesis and semantic vector embeddings (`text-embedding-004`).
   - `cache.SemanticCacheService`: In-memory cosine similarity caching (>0.88 threshold) providing sub-50ms responses for recurring or related architectural queries.
   - `builder.SampleBuilderEngine` & `builder.BuilderEngine`: Translates natural language prompts into paletted 16x16x16 voxel structures encoded as Base64.
5. **Local Verification**:
   ```powershell
   $env:PORT = "8080"
   .\mvnw.cmd spring-boot:run
   ```
   Verify endpoints:
   - `curl -s http://localhost:8080/health`
   - `curl -s -X POST http://localhost:8080/api/v1/build -H "Content-Type: application/json" -d "{\"prompt\":\"medieval watchtower\"}"`
6. **Commit and Push**:
   - Commit message: What changed and why, tests added, and `refs #N` or `closes #N`.
   - End commit message with agent co-author line: `Co-authored-by: Antigravity <antigravity@google.com>` (or session agent).
   - Rebase on `origin/main` before pushing: `git pull --rebase origin main; git push origin main`.
7. **Cloud Run Deployment**:
   - Production URL: `https://minecraft-workshop-server-481791752877.us-west2.run.app`
   - Deploy command (gcloud CLI):
     ```bash
     gcloud run deploy minecraft-workshop-server --source . --region us-west2 --allow-unauthenticated
     ```
   - Verify health check after deployment:
     ```powershell
     curl -s https://minecraft-workshop-server-481791752877.us-west2.run.app/health
     ```

## Secrets
- The `GEMINI_API_KEY` is provided via Cloud Run environment secrets.
- Never hardcode, log, print, or commit API keys or secret tokens.
