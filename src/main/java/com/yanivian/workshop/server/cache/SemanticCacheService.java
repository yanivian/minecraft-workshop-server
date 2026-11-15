package com.yanivian.workshop.server.cache;

import com.yanivian.workshop.server.builder.BuildResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory semantic vector cache for AI structure generation.
 * Maps prompt embeddings to precomputed voxel structures using cosine similarity.
 */
public class SemanticCacheService {

    public record CacheEntry(
        String prompt,
        float[] embedding,
        BuildResult result
    ) {}

    public record MatchResult(
        BuildResult result,
        double similarityScore
    ) {}

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final double similarityThreshold;

    public SemanticCacheService(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public SemanticCacheService() {
        this(0.88); // 88% cosine similarity threshold for architectural intent match
    }

    public void put(String prompt, float[] embedding, BuildResult result) {
        if (prompt == null || embedding == null || result == null) return;
        String key = normalizePrompt(prompt);
        cache.put(key, new CacheEntry(prompt, embedding, result));
    }

    public MatchResult findBestMatch(String prompt, float[] embedding) {
        if (cache.isEmpty()) {
            return null;
        }

        // 1. Exact string match fast-path
        String key = normalizePrompt(prompt);
        CacheEntry exact = cache.get(key);
        if (exact != null) {
            return new MatchResult(exact.result(), 1.0);
        }

        if (embedding == null || embedding.length == 0) {
            return null;
        }

        // 2. Semantic vector cosine similarity scan
        CacheEntry bestEntry = null;
        double bestSimilarity = -1.0;

        for (CacheEntry entry : cache.values()) {
            double sim = cosineSimilarity(embedding, entry.embedding());
            if (sim > bestSimilarity) {
                bestSimilarity = sim;
                bestEntry = entry;
            }
        }

        if (bestEntry != null && bestSimilarity >= similarityThreshold) {
            return new MatchResult(bestEntry.result(), bestSimilarity);
        }

        return null;
    }

    public int size() {
        return cache.size();
    }

    public static double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA <= 0.0 || normB <= 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static String normalizePrompt(String prompt) {
        if (prompt == null) return "";
        return prompt.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "");
    }
}
