package com.yanivian.workshop.server.builder;

import java.util.List;

public record BuildResult(
    String structureId,
    String title,
    String story,
    int width,
    int height,
    int depth,
    List<String> palette,
    byte[] voxels
) {}
