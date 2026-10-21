package me.slopggobblercobbler.chunkaligner.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable 2D chunk coordinate representation with boundary calculations and spatial operations.
 */
public record ChunkPos2i(int x, int z) {

    /**
     * Chunk position from world block coordinates.
     */
    public static ChunkPos2i fromBlock(int blockX, int blockZ) {
        return new ChunkPos2i(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16));
    }

    /**
     * Chunk position from BlockPos3i.
     */
    public static ChunkPos2i fromBlock(BlockPos3i pos) {
        return new ChunkPos2i(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Minimum world block X coordinate (inclusive).
     */
    public int minBlockX() {
        return x * 16;
    }

    /**
     * Maximum world block X coordinate (inclusive).
     */
    public int maxBlockX() {
        return minBlockX() + 15;
    }

    /**
     * Minimum world block Z coordinate (inclusive).
     */
    public int minBlockZ() {
        return z * 16;
    }

    /**
     * Maximum world block Z coordinate (inclusive).
     */
    public int maxBlockZ() {
        return minBlockZ() + 15;
    }

    /**
     * Center world block X coordinate.
     */
    public int centerBlockX() {
        return minBlockX() + 8;
    }

    /**
     * Center world block Z coordinate.
     */
    public int centerBlockZ() {
        return minBlockZ() + 8;
    }

    /**
     * Chunk distance (Chebyshev / King move distance).
     */
    public int chebyshevDistance(ChunkPos2i other) {
        return Math.max(Math.abs(this.x - other.x), Math.abs(this.z - other.z));
    }

    /**
     * Chunk Manhattan distance (Taxicab).
     */
    public int manhattanDistance(ChunkPos2i other) {
        return Math.abs(this.x - other.x) + Math.abs(this.z - other.z);
    }

    /**
     * Euclidean chunk distance.
     */
    public double distance(ChunkPos2i other) {
        int dx = this.x - other.x;
        int dz = this.z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Corresponding Nether chunk coordinate.
     */
    public ChunkPos2i toNetherChunk() {
        return new ChunkPos2i(Math.floorDiv(minBlockX(), 128), Math.floorDiv(minBlockZ(), 128));
    }

    /**
     * Overworld chunk region (8x8 chunks) corresponding to this Nether chunk.
     */
    public List<ChunkPos2i> toOverworldChunks() {
        List<ChunkPos2i> list = new ArrayList<>(64);
        int baseChunkX = this.x * 8;
        int baseChunkZ = this.z * 8;
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                list.add(new ChunkPos2i(baseChunkX + dx, baseChunkZ + dz));
            }
        }
        return list;
    }

    /**
     * 4-directional cardinal neighbor chunks.
     */
    public List<ChunkPos2i> cardinalNeighbors() {
        return List.of(
            new ChunkPos2i(x + 1, z),
            new ChunkPos2i(x - 1, z),
            new ChunkPos2i(x, z + 1),
            new ChunkPos2i(x, z - 1)
        );
    }

    /**
     * 8-directional Moore neighbor chunks.
     */
    public List<ChunkPos2i> mooreNeighbors() {
        List<ChunkPos2i> neighbors = new ArrayList<>(8);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                neighbors.add(new ChunkPos2i(x + dx, z + dz));
            }
        }
        return neighbors;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", x, z);
    }
}
