package com.yanivian.workshop.server;

import com.yanivian.workshop.server.api.BuildRequest;
import com.yanivian.workshop.server.builder.BuildResult;
import com.yanivian.workshop.server.builder.SampleBuilderEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SampleBuilderEngineTest {

    @Test
    public void testGenerateWatchtower() {
        SampleBuilderEngine engine = new SampleBuilderEngine();
        BuildResult result = engine.generate(new BuildRequest("watchtower", "medieval", 1));

        assertNotNull(result);
        assertEquals("Medieval Stone Watchtower", result.title());
        assertEquals(16, result.width());
        assertEquals(16, result.height());
        assertEquals(16, result.depth());
        assertEquals(4096, result.voxels().length);
        assertTrue(result.palette().contains("stone_bricks"));
        assertTrue(result.palette().contains("cobblestone"));

        // Verify non-empty voxel placement
        int solidCount = 0;
        for (byte b : result.voxels()) {
            if (b > 0) solidCount++;
        }
        assertTrue(solidCount > 100, "Structure should contain solid voxels");
    }

    @Test
    public void testGenerateCottage() {
        SampleBuilderEngine engine = new SampleBuilderEngine();
        BuildResult result = engine.generate(new BuildRequest("cozy cottage", "rustic", 1));

        assertNotNull(result);
        assertEquals("Rustic Timber Cottage", result.title());
        assertTrue(result.palette().contains("oak_planks"));
    }
}
