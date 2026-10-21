package me.slopggobblercobbler.chunkaligner.math;

/**
 * Encapsulates bidirectional Nether/Overworld portal grid snapping and linking calculations.
 */
public record PortalLinkResult(
    BlockPos3i overworldPos,
    BlockPos3i netherPos,
    BlockPos3i frameDimensions,
    boolean overworldContainedInSingleChunk,
    boolean netherContainedInSingleChunk,
    BlockPos3i netherSearchMin,
    BlockPos3i netherSearchMax,
    BlockPos3i overworldSearchMin,
    BlockPos3i overworldSearchMax,
    boolean bidirectionalExactMatch,
    int overworldChunkCrossingBlocks,
    int netherChunkCrossingBlocks
) {
    /**
     * Checks if this portal placement is completely leak-free (no chunk crossing in either dimension).
     */
    public boolean isOptimal() {
        return overworldContainedInSingleChunk && netherContainedInSingleChunk && bidirectionalExactMatch;
    }
}
