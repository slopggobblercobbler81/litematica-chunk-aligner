package me.slopggobblercobbler.chunkaligner.math;

import java.util.Objects;

/**
 * Immutable 3D block position representation with chunk grid and dimensional translation mathematics.
 */
public record BlockPos3i(int x, int y, int z) {

    public static final BlockPos3i ORIGIN = new BlockPos3i(0, 0, 0);

    /**
     * Chunk coordinate on X axis: floor(x / 16).
     */
    public int chunkX() {
        return Math.floorDiv(x, 16);
    }

    /**
     * Sub-chunk vertical index: floor(y / 16).
     */
    public int subChunkY() {
        return Math.floorDiv(y, 16);
    }

    /**
     * Chunk coordinate on Z axis: floor(z / 16).
     */
    public int chunkZ() {
        return Math.floorDiv(z, 16);
    }

    /**
     * In-chunk local X coordinate [0..15].
     */
    public int localX() {
        return Math.floorMod(x, 16);
    }

    /**
     * In-sub-chunk local Y coordinate [0..15].
     */
    public int localY() {
        return Math.floorMod(y, 16);
    }

    /**
     * In-chunk local Z coordinate [0..15].
     */
    public int localZ() {
        return Math.floorMod(z, 16);
    }

    /**
     * Absolute block X for chunk minimum boundary (North-West corner).
     */
    public int chunkMinX() {
        return chunkX() * 16;
    }

    /**
     * Absolute block X for chunk maximum boundary (South-East corner, inclusive).
     */
    public int chunkMaxX() {
        return chunkMinX() + 15;
    }

    /**
     * Absolute block Z for chunk minimum boundary.
     */
    public int chunkMinZ() {
        return chunkZ() * 16;
    }

    /**
     * Absolute block Z for chunk maximum boundary (inclusive).
     */
    public int chunkMaxZ() {
        return chunkMinZ() + 15;
    }

    /**
     * Absolute block Y for sub-chunk bottom boundary.
     */
    public int subChunkMinY() {
        return subChunkY() * 16;
    }

    /**
     * Absolute block Y for sub-chunk top boundary (inclusive).
     */
    public int subChunkMaxY() {
        return subChunkMinY() + 15;
    }

    /**
     * Convert Overworld coordinates to exact Nether linked coordinates (8:1 ratio).
     */
    public BlockPos3i toNether() {
        return new BlockPos3i(Math.floorDiv(x, 8), y, Math.floorDiv(z, 8));
    }

    /**
     * Convert Nether coordinates to Overworld coordinates (1:8 ratio).
     */
    public BlockPos3i toOverworld() {
        return new BlockPos3i(x * 8, y, z * 8);
    }

    /**
     * Offset position by given delta.
     */
    public BlockPos3i offset(int dx, int dy, int dz) {
        if (dx == 0 && dy == 0 && dz == 0) return this;
        return new BlockPos3i(x + dx, y + dy, z + dz);
    }

    /**
     * Euclidean distance squared between two block positions.
     */
    public double distanceSq(BlockPos3i other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Euclidean distance between two block positions.
     */
    public double distance(BlockPos3i other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Manhattan distance (L1 norm).
     */
    public int manhattanDistance(BlockPos3i other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }
}
