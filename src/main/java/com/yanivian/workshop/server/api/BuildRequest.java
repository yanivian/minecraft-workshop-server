package com.yanivian.workshop.server.api;

public record BuildRequest(
    String prompt,
    String style,
    Integer seed
) {}
