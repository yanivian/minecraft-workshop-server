package com.yanivian.workshop.server.builder;

import com.yanivian.workshop.server.api.BuildRequest;

import java.util.List;
import java.util.UUID;

/**
 * Baseline procedural builder engine supplying authentic sample structures:
 * - Medieval Watchtower (with open walkthrough doorway and crenelated parapet)
 * - Rustic Timber Cottage (with open doorway, oak plank floor, and overhang gable roof)
 * - Tranquil Garden with Oak Tree (with leafy canopy, winding path, and flowers)
 */
public class SampleBuilderEngine implements BuilderEngine {

    @Override
    public BuildResult generate(BuildRequest request) {
        String prompt = request.prompt() != null ? request.prompt().toLowerCase() : "";

        if (prompt.contains("cottage") || prompt.contains("house")) {
            return generateCottage();
        } else if (prompt.contains("garden") || prompt.contains("tree") || prompt.contains("fountain") || prompt.contains("park")) {
            return generateGarden();
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
        // Front doorstep
        setVoxel(voxels, 7, 0, 2, 1);
        setVoxel(voxels, 8, 0, 2, 1);

        // 2. Corner Pillars (Oak Log)
        for (int y = 1; y <= 11; y++) {
            setVoxel(voxels, 3, y, 3, 2);
            setVoxel(voxels, 12, y, 3, 2);
            setVoxel(voxels, 3, y, 12, 2);
            setVoxel(voxels, 12, y, 12, 2);
        }

        // 3. Walls (Stone Bricks)
        for (int y = 1; y <= 10; y++) {
            for (int x = 4; x <= 11; x++) {
                setVoxel(voxels, x, y, 3, 3);
                setVoxel(voxels, x, y, 12, 3);
            }
            for (int z = 4; z <= 11; z++) {
                setVoxel(voxels, 3, y, z, 3);
                setVoxel(voxels, 12, y, z, 3);
            }
        }

        // Open Walkthrough Doorway on front wall (z = 3) at x = 7, 8 for y = 1, 2, 3
        setVoxel(voxels, 7, 1, 3, 0);
        setVoxel(voxels, 8, 1, 3, 0);
        setVoxel(voxels, 7, 2, 3, 0);
        setVoxel(voxels, 8, 2, 3, 0);
        setVoxel(voxels, 7, 3, 3, 0);
        setVoxel(voxels, 8, 3, 3, 0);
        // Lintel beam above doorway
        setVoxel(voxels, 7, 4, 3, 2);
        setVoxel(voxels, 8, 4, 3, 2);

        // Windows (Glass) on upper level (y = 6)
        setVoxel(voxels, 5, 6, 3, 4);
        setVoxel(voxels, 10, 6, 3, 4);
        setVoxel(voxels, 7, 6, 12, 4);
        setVoxel(voxels, 8, 6, 12, 4);
        setVoxel(voxels, 3, 6, 7, 4);
        setVoxel(voxels, 12, 6, 7, 4);

        // 4. Overhanging Lookout Platform (Oak Planks & Stone Bricks)
        for (int x = 2; x <= 13; x++) {
            for (int z = 2; z <= 13; z++) {
                setVoxel(voxels, x, 11, z, 5);
            }
        }
        // Battlements crenellations around perimeter
        for (int x = 2; x <= 13; x += 2) {
            setVoxel(voxels, x, 12, 2, 3);
            setVoxel(voxels, x, 12, 13, 3);
        }
        for (int z = 2; z <= 13; z += 2) {
            setVoxel(voxels, 2, 12, z, 3);
            setVoxel(voxels, 13, 12, z, 3);
        }

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Medieval Stone Watchtower",
            "A sturdy medieval guard outpost with oak framing, stone brick curtain walls, open walkthrough doorway, and crenelated parapet.",
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

        // Foundation and interior floor
        for (int x = 3; x <= 12; x++) {
            for (int z = 3; z <= 12; z++) {
                setVoxel(voxels, x, 0, z, 1);
            }
        }
        for (int x = 4; x <= 11; x++) {
            for (int z = 4; z <= 11; z++) {
                setVoxel(voxels, x, 0, z, 3); // Oak planks interior floor
            }
        }
        // Doorstep
        setVoxel(voxels, 7, 0, 2, 1);
        setVoxel(voxels, 8, 0, 2, 1);

        // Walls (Oak Planks with Log Corners) on y: 1..4
        for (int y = 1; y <= 4; y++) {
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

        // Open Walkthrough Doorway on front wall (z = 3) at x = 7, 8 for y = 1, 2
        setVoxel(voxels, 7, 1, 3, 0);
        setVoxel(voxels, 8, 1, 3, 0);
        setVoxel(voxels, 7, 2, 3, 0);
        setVoxel(voxels, 8, 2, 3, 0);
        // Wooden lintel beam
        setVoxel(voxels, 7, 3, 3, 2);
        setVoxel(voxels, 8, 3, 2, 2);

        // Windows
        setVoxel(voxels, 5, 2, 3, 5);
        setVoxel(voxels, 10, 2, 3, 5);
        setVoxel(voxels, 5, 2, 12, 5);
        setVoxel(voxels, 10, 2, 12, 5);
        setVoxel(voxels, 3, 2, 7, 5);
        setVoxel(voxels, 12, 2, 7, 5);

        // Proper Pitched Gable Roof with Eaves and Overhangs (running z: 2..13)
        int zMin = 2, zMax = 13;

        // Eaves level (y = 4): overhang on x = 2 and x = 13
        for (int z = zMin; z <= zMax; z++) {
            setVoxel(voxels, 2, 4, z, 1);
            setVoxel(voxels, 13, 4, z, 1);
        }

        // Roof slope 1 (y = 5): x = 3 and x = 12
        for (int z = zMin; z <= zMax; z++) {
            boolean isBarge = (z == zMin || z == zMax);
            setVoxel(voxels, 3, 5, z, isBarge ? 1 : 4);
            setVoxel(voxels, 12, 5, z, isBarge ? 1 : 4);
        }
        // Triangular gable wall fill at y = 5
        for (int x = 4; x <= 11; x++) {
            setVoxel(voxels, x, 5, 3, 3);
            setVoxel(voxels, x, 5, 12, 3);
        }

        // Roof slope 2 (y = 6): x = 4 and x = 11
        for (int z = zMin; z <= zMax; z++) {
            boolean isBarge = (z == zMin || z == zMax);
            setVoxel(voxels, 4, 6, z, isBarge ? 1 : 4);
            setVoxel(voxels, 11, 6, z, isBarge ? 1 : 4);
        }
        // Triangular gable wall fill at y = 6
        for (int x = 5; x <= 10; x++) {
            setVoxel(voxels, x, 6, 3, 3);
            setVoxel(voxels, x, 6, 12, 3);
        }

        // Roof slope 3 (y = 7): x = 5 and x = 10
        for (int z = zMin; z <= zMax; z++) {
            boolean isBarge = (z == zMin || z == zMax);
            setVoxel(voxels, 5, 7, z, isBarge ? 1 : 4);
            setVoxel(voxels, 10, 7, z, isBarge ? 1 : 4);
        }

        // Ridge cap (y = 8): x: 6..9
        for (int z = zMin; z <= zMax; z++) {
            boolean isBarge = (z == zMin || z == zMax);
            for (int x = 6; x <= 9; x++) {
                setVoxel(voxels, x, 8, z, isBarge ? 1 : 4);
            }
        }

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Rustic Timber Cottage",
            "A cozy countryside cottage with timber framing, open doorway, oak floor, and authentic pitched spruce gable roof with eaves.",
            16, 16, 16,
            palette,
            voxels
        );
    }

    private BuildResult generateGarden() {
        int size = 16;
        byte[] voxels = new byte[size * size * size];

        List<String> palette = List.of(
            "air",              // 0
            "stone_bricks",     // 1
            "cobblestone",      // 2
            "oak_log",          // 3
            "oak_leaves",       // 4
            "spruce_planks",    // 5
            "poppy",            // 6
            "dandelion"         // 7
        );

        // 1. Garden perimeter border wall (stone bricks) with open entrance
        for (int x = 1; x <= 14; x++) {
            setVoxel(voxels, x, 0, 14, 1);
            if (x < 7 || x > 8) {
                setVoxel(voxels, x, 0, 1, 1);
            }
        }
        for (int z = 1; z <= 14; z++) {
            setVoxel(voxels, 1, 0, z, 1);
            setVoxel(voxels, 14, 0, z, 1);
        }
        // Corner pillars
        setVoxel(voxels, 1, 1, 1, 1);
        setVoxel(voxels, 14, 1, 1, 1);
        setVoxel(voxels, 1, 1, 14, 1);
        setVoxel(voxels, 14, 1, 14, 1);

        // 2. Cobblestone path
        for (int z = 1; z <= 4; z++) {
            setVoxel(voxels, 7, 0, z, 2);
            setVoxel(voxels, 8, 0, z, 2);
        }
        for (int x = 4; x <= 6; x++) {
            setVoxel(voxels, x, 0, 4, 2);
        }

        // 3. Timber Garden Bench
        setVoxel(voxels, 3, 0, 5, 3);
        setVoxel(voxels, 3, 0, 6, 3);
        setVoxel(voxels, 3, 1, 5, 5);
        setVoxel(voxels, 3, 1, 6, 5);

        // 4. Magnificent Oak Tree centered at (8, y, 9)
        for (int y = 0; y <= 6; y++) {
            setVoxel(voxels, 8, y, 9, 3);
            setVoxel(voxels, 9, y, 9, 3);
        }
        // Roots
        setVoxel(voxels, 7, 0, 9, 3);
        setVoxel(voxels, 10, 0, 9, 3);
        setVoxel(voxels, 8, 0, 8, 3);
        setVoxel(voxels, 9, 0, 10, 3);

        // Multi-tier Oak Leaves Canopy
        for (int y = 4; y <= 5; y++) {
            for (int x = 6; x <= 11; x++) {
                for (int z = 7; z <= 12; z++) {
                    boolean isCorner = (x == 6 || x == 11) && (z == 7 || z == 12);
                    if (!isCorner && getVoxel(voxels, x, y, z) == 0) {
                        setVoxel(voxels, x, y, z, 4);
                    }
                }
            }
        }
        for (int x = 7; x <= 10; x++) {
            for (int z = 8; z <= 11; z++) {
                if (getVoxel(voxels, x, 6, z) == 0) {
                    setVoxel(voxels, x, 6, z, 4);
                }
            }
        }
        setVoxel(voxels, 8, 7, 9, 4);
        setVoxel(voxels, 9, 7, 9, 4);
        setVoxel(voxels, 8, 7, 10, 4);
        setVoxel(voxels, 9, 7, 10, 4);

        // 5. Flowers
        setVoxel(voxels, 4, 0, 2, 6); // Poppy
        setVoxel(voxels, 10, 0, 2, 6); // Poppy
        setVoxel(voxels, 5, 0, 2, 7); // Dandelion
        setVoxel(voxels, 11, 0, 3, 7); // Dandelion
        setVoxel(voxels, 12, 0, 6, 6); // Poppy
        setVoxel(voxels, 12, 0, 11, 7); // Dandelion

        return new BuildResult(
            UUID.randomUUID().toString(),
            "Tranquil Garden & Oak Tree",
            "A serene walled garden with an oak tree, lush foliage canopy, winding stone path, flowerbeds, and timber bench.",
            16, 16, 16,
            palette,
            voxels
        );
    }

    private void setVoxel(byte[] voxels, int x, int y, int z, int val) {
        if (x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) return;
        voxels[x + y * 16 + z * 16 * 16] = (byte) val;
    }

    private byte getVoxel(byte[] voxels, int x, int y, int z) {
        if (x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) return 0;
        return voxels[x + y * 16 + z * 16 * 16];
    }
}
