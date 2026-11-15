package com.yanivian.workshop.server.api;

public record HealthResponse(
    String status,
    String service,
    String version,
    long uptimeMs
) {}
