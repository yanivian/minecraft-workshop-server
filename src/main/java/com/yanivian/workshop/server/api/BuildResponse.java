package com.yanivian.workshop.server.api;

import java.util.List;

public record BuildResponse(
    String structureId,
    String title,
    String story,
    int width,
    int height,
    int depth,
    List<String> palette,
    String voxelDataB64,
    boolean cached,
    double similarityScore,
    long generationTimeMs
) {}
