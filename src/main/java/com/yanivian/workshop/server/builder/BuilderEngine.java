package com.yanivian.workshop.server.builder;

import com.yanivian.workshop.server.api.BuildRequest;

/**
 * Pluggable contract for procedural structure generation engines.
 * Acts as the bridge between cloud API routing and the core architectural grammar.
 */
public interface BuilderEngine {
    BuildResult generate(BuildRequest request);
}
