package com.yanivian.workshop.server.api;

public record FavoriteItem(
    String id,
    String name,
    String category,
    String description,
    String thumbnailPrompt
) {}
