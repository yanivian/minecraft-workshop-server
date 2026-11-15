package com.yanivian.workshop.server.builder;

import com.yanivian.workshop.server.api.BuildRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Baseline procedural builder engine for immediate testing and verification
 * while the full grammar compiler from minecraft-workshop is being prepared for migration.
 */
public class SampleBuilderEngine implements BuilderEngine {

    @Override
    public BuildResult generate(BuildRequest request) {
        String prompt = request.prompt() != null ? request.prompt().toLowerCase() : "";

        if (prompt.contains("cottage") || prompt.contains("house")) {
            return generateCottage();
        } else if (prompt.contains("fountain") || prompt.contains("water")) {
            return generateFountain();
        } else {
            return generateWatchtower();
        }
    }

    private BuildResult generateWatchtower() {
        int size = 16;
        byte[] voxels = new byte[size * size * size];

        List<String> palette = List.of(
            "air",              // 0
            "cobblestone",      // 1
            "oak_log",          // 2
            "stone_bricks",     // 3
            "glass",            // 4
            "oak_planks"        // 5
        );

        // 1. Cobblestone Plinth
        for (int x = 2; x <= 13; x++) {
            for (int z = 2; z <= 13; z++) {
                setVoxel(voxels, x, 0, z, 1);
            }
        }

        // 2. Corner Pillars (Oak Log)
        for (int y = 1; y <= 12; y++) {
            setVoxel(voxels, 3, y, 3, 2);
            setVoxel(voxels, 12, y, 3, 2);
            setVoxel(voxels, 3, y, 12, 2);
            setVoxel(voxels, 12, y, 12, 2);
        }

        // 3. Walls (Stone Bricks)
        for (int y = 1; y <= 11; y++) {
            for (int x = 4; x <= 11; x++) {
                setVoxel(voxels, x, y, 3, 3);
                setVoxel(voxels, x, y, 12, 3);
            }
            for (int z = 4; z <= 11; z++) {
                setVoxel(voxels, 3, y, z, 3);
                setVoxel(voxels, 12, y, z, 3);
            }
        }

        // 4. Windows (Glass)
        setVoxel(voxels, 7, 6, 3, 4);
        setVoxel(voxels, 8, 6, 3, 4);
        setVoxel(voxels, 7, 6, 12, 4);
        setVoxel(voxels, 8, 6, 12, 4);

        // 5. Floor & Battlements (Oak Planks & Stone Bricks)
        for (int x = 2; x <= 13; x++) {
            for (int z = 2; z <= 13; z++) {
                setVoxel(voxels, x, 12, z, 5);
            }
        }
        for (int x = 2; x <= 13; x += 2) {
            setVoxel(voxels, x, 13, 2, 3);
            setVoxel(voxels, x, 13, 13, 3);
        }
        for (int z = 2; z <= 13; z += 2) {
            setVoxel(voxels, 2, 13, z, 3);
            setVoxel(voxels, 13, 13, z, 3);
        }

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Medieval Stone Watchtower",
            "A sturdy medieval guard outpost with oak framing, stone brick curtain walls, and crenelated parapet.",
            16, 16, 16,
            palette,
            voxels
        );
    }

    private BuildResult generateCottage() {
        int size = 16;
        byte[] voxels = new byte[size * size * size];

        List<String> palette = List.of(
            "air",              // 0
            "cobblestone",      // 1
            "oak_log",          // 2
            "oak_planks",       // 3
            "spruce_planks",    // 4
            "glass"             // 5
        );

        // Foundation
        for (int x = 3; x <= 12; x++) {
            for (int z = 3; z <= 12; z++) {
                setVoxel(voxels, x, 0, z, 1);
            }
        }

        // Walls
        for (int y = 1; y <= 5; y++) {
            for (int x = 3; x <= 12; x++) {
                setVoxel(voxels, x, y, 3, 3);
                setVoxel(voxels, x, y, 12, 3);
            }
            for (int z = 3; z <= 12; z++) {
                setVoxel(voxels, 3, y, z, 3);
                setVoxel(voxels, 12, y, z, 3);
            }
            // Corner logs
            setVoxel(voxels, 3, y, 3, 2);
            setVoxel(voxels, 12, y, 3, 2);
            setVoxel(voxels, 3, y, 12, 2);
            setVoxel(voxels, 12, y, 12, 2);
        }

        // Gable Roof
        for (int step = 0; step <= 3; step++) {
            int y = 6 + step;
            int x1 = 3 + step;
            int x2 = 12 - step;
            for (int z = 2; z <= 13; z++) {
                setVoxel(voxels, x1, y, z, 4);
                setVoxel(voxels, x2, y, z, 4);
            }
        }

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Rustic Timber Cottage",
            "A cozy countryside cottage with timber framing, spruce pitched roof, and cobblestone foundation.",
            16, 16, 16,
            palette,
            voxels
        );
    }

    private BuildResult generateFountain() {
        int size = 16;
        byte[] voxels = new byte[size * size * size];

        List<String> palette = List.of(
            "air",              // 0
            "stone_bricks",     // 1
            "deepslate_bricks", // 2
            "glass"             // 3 (representing water in voxel palette)
        );

        // Basin rim
        for (int x = 3; x <= 12; x++) {
            setVoxel(voxels, x, 0, 3, 1);
            setVoxel(voxels, x, 0, 12, 1);
        }
        for (int z = 3; z <= 12; z++) {
            setVoxel(voxels, 3, 0, z, 1);
            setVoxel(voxels, 12, 0, z, 1);
        }

        // Center Pillar
        for (int y = 0; y <= 6; y++) {
            setVoxel(voxels, 7, y, 7, 2);
            setVoxel(voxels, 8, y, 7, 2);
            setVoxel(voxels, 7, y, 8, 2);
            setVoxel(voxels, 8, y, 8, 2);
        }

        // Top basin
        for (int x = 6; x <= 9; x++) {
            for (int z = 6; z <= 9; z++) {
                setVoxel(voxels, x, 5, z, 1);
            }
        }

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Courtyard Stone Fountain",
            "An ornate multi-tiered stone brick fountain designed for town squares and castle courtyards.",
            16, 16, 16,
            palette,
            voxels
        );
    }

    private void setVoxel(byte[] voxels, int x, int y, int z, int val) {
        if (x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) return;
        voxels[x + y * 16 + z * 16 * 16] = (byte) val;
    }
}
