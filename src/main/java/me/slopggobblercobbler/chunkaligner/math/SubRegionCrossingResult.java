package me.slopggobblercobbler.chunkaligner.math;

import java.util.List;

/**
 * Detailed chunk intersection analysis for a Litematica schematic sub-region.
 */
public record SubRegionCrossingResult(
    String regionName,
    BlockPos3i worldOrigin,
    BlockPos3i dimensions,
    int chunkSpanX,
    int chunkSpanZ,
    int totalChunksIntersected,
    List<ChunkPos2i> intersectedChunks,
    int borderCrossingsX,
    int borderCrossingsZ
) {
    /**
     * True if the sub-region is contained within a single chunk column.
     */
    public boolean isContainedInSingleChunk() {
        return totalChunksIntersected == 1;
    }
}
