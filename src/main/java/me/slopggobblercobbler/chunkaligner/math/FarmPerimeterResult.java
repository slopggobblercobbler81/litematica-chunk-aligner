package me.slopggobblercobbler.chunkaligner.math;

/**
 * Result data for mob farm perimeter calculations including chunk bounding box and AFK positioning.
 */
public record FarmPerimeterResult(
    BlockPos3i minBlock,
    BlockPos3i maxBlock,
    ChunkPos2i minChunk,
    ChunkPos2i maxChunk,
    int chunkWidth,
    int chunkLength,
    int totalChunks,
    BlockPos3i afkPosition,
    int despawnRadius,
    double subChunkSpawningEfficiencyFactor,
    int subChunkLayerY
) {
    /**
     * Checks if a point in 3D space falls within the hostile mob despawn sphere (128m).
     */
    public boolean isWithinDespawnSphere(BlockPos3i point) {
        return afkPosition.distanceSq(point) <= (double) (despawnRadius * despawnRadius);
    }

    /**
     * Checks if a point is outside the minimum spawn distance (24 blocks from AFK position).
     */
    public boolean isValidSpawnDistance(BlockPos3i point) {
        double distSq = afkPosition.distanceSq(point);
        return distSq >= (24.0 * 24.0) && distSq <= (double) (despawnRadius * despawnRadius);
    }
}
