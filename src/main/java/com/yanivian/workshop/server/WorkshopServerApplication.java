package com.yanivian.workshop.server;

import com.google.gson.Gson;
import com.yanivian.workshop.server.ai.GeminiClient;
import com.yanivian.workshop.server.api.BuildRequest;
import com.yanivian.workshop.server.api.BuildResponse;
import com.yanivian.workshop.server.api.FavoriteItem;
import com.yanivian.workshop.server.api.HealthResponse;
import com.yanivian.workshop.server.builder.BuildResult;
import com.yanivian.workshop.server.builder.BuilderEngine;
import com.yanivian.workshop.server.builder.SampleBuilderEngine;
import com.yanivian.workshop.server.cache.SemanticCacheService;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class WorkshopServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopServerApplication.class);
    private static final long startTimeMs = System.currentTimeMillis();
    private static final Gson gson = new Gson();

    private final BuilderEngine builderEngine;
    private final SemanticCacheService cacheService;
    private final GeminiClient geminiClient;

    public WorkshopServerApplication(BuilderEngine builderEngine) {
        this.builderEngine = builderEngine;
        this.cacheService = new SemanticCacheService();
        this.geminiClient = new GeminiClient(System.getenv("GEMINI_API_KEY"));

        // Seed cache with baseline structures
        seedInitialCache();
    }

    public static void main(String[] args) {
        int port = getPort();
        logger.info("Starting minecraft-workshop-server on port {}", port);

        // Scaffolded with pluggable builder engine (SampleBuilderEngine until compiler is moved)
        BuilderEngine engine = new SampleBuilderEngine();
        WorkshopServerApplication app = new WorkshopServerApplication(engine);
        app.start(port);
    }

    public Javalin start(int port) {
        Javalin server = Javalin.create(config -> {
            config.jsonMapper(new io.javalin.json.JsonMapper() {
                @Override
                public String toJsonString(Object obj, java.lang.reflect.Type type) {
                    return gson.toJson(obj, type);
                }

                @Override
                public <T> T fromJsonString(String json, java.lang.reflect.Type targetType) {
                    return gson.fromJson(json, targetType);
                }
            });
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> rule.anyHost());
            });
        });

        // 1. Health check
        server.get("/health", ctx -> {
            HealthResponse resp = new HealthResponse(
                "ok",
                "minecraft-workshop-server",
                "1.0.0",
                System.currentTimeMillis() - startTimeMs
            );
            ctx.json(resp);
        });

        // 2. Build API
        server.post("/api/v1/build", ctx -> {
            long t0 = System.currentTimeMillis();
            BuildRequest req = ctx.bodyAsClass(BuildRequest.class);

            if (req.prompt() == null || req.prompt().isBlank()) {
                ctx.status(400).result("Prompt parameter is required");
                return;
            }

            // A. Check Semantic Cache
            float[] embedding = geminiClient.getEmbedding(req.prompt());
            SemanticCacheService.MatchResult cached = cacheService.findBestMatch(req.prompt(), embedding);

            BuildResult result;
            boolean fromCache;
            double score = 1.0;

            if (cached != null) {
                result = cached.result();
                fromCache = true;
                score = cached.similarityScore();
                logger.info("Semantic cache HIT for '{}' (similarity: {})", req.prompt(), String.format("%.2f", score));
            } else {
                result = builderEngine.generate(req);
                fromCache = false;
                score = 1.0;
                cacheService.put(req.prompt(), embedding, result);
                logger.info("Generated new build for '{}' in {}ms", req.prompt(), System.currentTimeMillis() - t0);
            }

            long elapsed = System.currentTimeMillis() - t0;
            String voxelB64 = Base64.getEncoder().encodeToString(result.voxels());

            BuildResponse response = new BuildResponse(
                result.structureId(),
                result.title(),
                result.story(),
                result.width(),
                result.height(),
                result.depth(),
                result.palette(),
                voxelB64,
                fromCache,
                score,
                elapsed
            );

            ctx.json(response);
        });

        // 3. Favorites / Catalog API
        server.get("/api/v1/favorites", ctx -> {
            List<FavoriteItem> favorites = List.of(
                new FavoriteItem("watchtower", "Medieval Watchtower", "Defense", "A 16-block tall stone and timber observation tower.", "a medieval watchtower with arrow slits"),
                new FavoriteItem("cottage", "Rustic Timber Cottage", "Residential", "A cozy countryside house with a gabled spruce roof and oak floor.", "a small cozy timber cottage"),
                new FavoriteItem("garden", "Tranquil Garden & Oak Tree", "Nature", "A serene garden with a leafy oak tree, stone path, flowerbeds, and bench.", "a peaceful garden with a leafy oak tree and flowers")
            );
            ctx.json(favorites);
        });

        server.start(port);
        logger.info("minecraft-workshop-server ready at http://localhost:{}", port);
        return server;
    }

    private void seedInitialCache() {
        logger.info("Seeding semantic cache with baseline architectural catalog...");
        BuildRequest reqTower = new BuildRequest("medieval stone watchtower", "medieval", 42);
        BuildResult resTower = builderEngine.generate(reqTower);
        cacheService.put("watchtower", geminiClient.getEmbedding("watchtower"), resTower);
        cacheService.put("stone watchtower", geminiClient.getEmbedding("stone watchtower"), resTower);
        cacheService.put("guard tower", geminiClient.getEmbedding("guard tower"), resTower);

        BuildRequest reqCottage = new BuildRequest("rustic timber cottage", "rustic", 42);
        BuildResult resCottage = builderEngine.generate(reqCottage);
        cacheService.put("cottage", geminiClient.getEmbedding("cottage"), resCottage);
        cacheService.put("house", geminiClient.getEmbedding("house"), resCottage);

        BuildRequest reqGarden = new BuildRequest("tranquil garden with oak tree", "nature", 42);
        BuildResult resGarden = builderEngine.generate(reqGarden);
        cacheService.put("garden", geminiClient.getEmbedding("garden"), resGarden);
        cacheService.put("tree", geminiClient.getEmbedding("tree"), resGarden);
        cacheService.put("park", geminiClient.getEmbedding("park"), resGarden);
    }

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }
}
