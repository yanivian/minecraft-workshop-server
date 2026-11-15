package com.yanivian.workshop.server;

import com.yanivian.workshop.server.ai.GeminiClient;
import com.yanivian.workshop.server.builder.BuildResult;
import com.yanivian.workshop.server.cache.SemanticCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticCacheServiceTest {

    private SemanticCacheService cache;

    @BeforeEach
    public void setup() {
        cache = new SemanticCacheService(0.85);
    }

    @Test
    public void testExactMatch() {
        BuildResult res = new BuildResult("1", "Tower", "Desc", 16, 16, 16, List.of("air"), new byte[4096]);
        float[] vec = GeminiClient.generatePseudoEmbedding("stone tower");

        cache.put("stone tower", vec, res);

        SemanticCacheService.MatchResult match = cache.findBestMatch("Stone Tower!", vec);
        assertNotNull(match);
        assertEquals("Tower", match.result().title());
        assertEquals(1.0, match.similarityScore());
    }

    @Test
    public void testCosineSimilarityComputation() {
        float[] v1 = new float[] { 1.0f, 0.0f, 0.0f };
        float[] v2 = new float[] { 1.0f, 0.0f, 0.0f };
        float[] v3 = new float[] { 0.0f, 1.0f, 0.0f };

        assertEquals(1.0, SemanticCacheService.cosineSimilarity(v1, v2), 0.001);
        assertEquals(0.0, SemanticCacheService.cosineSimilarity(v1, v3), 0.001);
    }

    @Test
    public void testNormalization() {
        assertEquals("stone watchtower", SemanticCacheService.normalizePrompt("  Stone Watchtower!!! "));
    }
}
