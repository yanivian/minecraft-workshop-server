package com.yanivian.workshop.server.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Lightweight Gemini API client using Java 21 HttpClient.
 * Interacts with Gemini 2.5 Flash for architectural reasoning and text-embedding-004 for semantic caching.
 */
public class GeminiClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public GeminiClient(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : System.getenv("GEMINI_API_KEY");
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generates a 768-dimensional semantic embedding using text-embedding-004.
     */
    public float[] getEmbedding(String text) {
        if (!hasApiKey() || text == null || text.isBlank()) {
            return generatePseudoEmbedding(text);
        }

        try {
            String endpoint = GEMINI_BASE_URL + "/text-embedding-004:embedContent?key=" + apiKey;

            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", text);
            parts.add(part);
            content.add("parts", parts);

            JsonObject body = new JsonObject();
            body.addProperty("model", "models/text-embedding-004");
            body.add("content", content);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .timeout(Duration.ofSeconds(8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                if (json.has("embedding")) {
                    JsonArray values = json.getAsJsonObject("embedding").getAsJsonArray("values");
                    float[] embedding = new float[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        embedding[i] = values.get(i).getAsFloat();
                    }
                    return embedding;
                }
            } else {
                logger.warn("Embedding API returned HTTP {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.error("Failed to query Gemini embedding API: {}", e.getMessage());
        }

        return generatePseudoEmbedding(text);
    }

    /**
     * Deterministic pseudo-embedding fallback for offline development or missing API keys.
     */
    public static float[] generatePseudoEmbedding(String text) {
        float[] vec = new float[768];
        if (text == null) return vec;

        String norm = text.toLowerCase().trim();
        for (int i = 0; i < norm.length(); i++) {
            int ch = norm.charAt(i);
            int idx = (ch * 31 + i * 17) % 768;
            vec[Math.abs(idx)] += 1.0f;
        }

        // Normalize vector to unit length
        double normSum = 0.0;
        for (float v : vec) normSum += v * v;
        if (normSum > 0.0) {
            double len = Math.sqrt(normSum);
            for (int i = 0; i < vec.length; i++) vec[i] /= len;
        }

        return vec;
    }
}
