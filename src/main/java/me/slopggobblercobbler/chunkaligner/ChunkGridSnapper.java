package me.slopggobblercobbler.chunkaligner;

import me.slopggobblercobbler.chunkaligner.math.BlockPos3i;
import me.slopggobblercobbler.chunkaligner.math.ChunkPos2i;
import me.slopggobblercobbler.chunkaligner.math.FarmPerimeterResult;
import me.slopggobblercobbler.chunkaligner.math.PortalLinkResult;
import me.slopggobblercobbler.chunkaligner.math.SubRegionCrossingResult;

import java.util.*;

/**
 * Core mathematical engine for chunk coordinate rounding, sub-chunk 16x16x16 grid snapping,
 * Nether portal 8:1 linking geometry, slime chunk RNG evaluation, and schematic placement optimization.
 */
public final class ChunkGridSnapper {

    /**
     * Anchor point definitions for snapping a bounding box to a chunk or sub-chunk.
     */
    public enum SnapAnchor {
        NORTH_WEST_BOTTOM,
        SOUTH_EAST_TOP,
        CENTER_HORIZONTAL,
        EXACT_ORIGIN
    }

    private ChunkGridSnapper() {
    }

    // =========================================================================
    // 1. SUB-CHUNK 16x16x16 SNAPPING
    // =========================================================================

    /**
     * Snaps an arbitrary placement position to the nearest chunk or sub-chunk grid.
     *
     * @param currentPos Current origin of the placement
     * @param dimensions Size of the schematic bounding box [width, height, length]
     * @param anchor Anchor mode to align with chunk boundary
     * @param snapVerticalSubChunk If true, rounds Y coordinate to nearest 16-block sub-chunk boundary
     * @return Snapped absolute BlockPos3i
     */
    public static BlockPos3i snapToSubChunk(BlockPos3i currentPos, BlockPos3i dimensions, SnapAnchor anchor, boolean snapVerticalSubChunk) {
        int chunkX = currentPos.chunkX();
        int chunkZ = currentPos.chunkZ();
        int subChunkY = currentPos.subChunkY();

        int targetX;
        int targetZ;
        int targetY;

        // Vertical snapping
        if (snapVerticalSubChunk) {
            targetY = subChunkY * 16;
        } else {
            targetY = currentPos.y();
        }

        switch (anchor) {
            case NORTH_WEST_BOTTOM -> {
                targetX = chunkX * 16;
                targetZ = chunkZ * 16;
            }
            case SOUTH_EAST_TOP -> {
                int dimX = Math.max(1, dimensions.x());
                int dimZ = Math.max(1, dimensions.z());
                int maxBlockX = (chunkX * 16) + 15;
                int maxBlockZ = (chunkZ * 16) + 15;
                targetX = maxBlockX - dimX + 1;
                targetZ = maxBlockZ - dimZ + 1;
            }
            case CENTER_HORIZONTAL -> {
                int dimX = Math.max(1, dimensions.x());
                int dimZ = Math.max(1, dimensions.z());
                int chunksNeededX = Math.max(1, (int) Math.ceil((double) dimX / 16.0));
                int chunksNeededZ = Math.max(1, (int) Math.ceil((double) dimZ / 16.0));
                int totalWidthX = chunksNeededX * 16;
                int totalLengthZ = chunksNeededZ * 16;

                int offsetX = (totalWidthX - dimX) / 2;
                int offsetZ = (totalLengthZ - dimZ) / 2;

                targetX = chunkX * 16 + offsetX;
                targetZ = chunkZ * 16 + offsetZ;
            }
            case EXACT_ORIGIN -> {
                targetX = chunkX * 16;
                targetZ = chunkZ * 16;
            }
            default -> {
                targetX = currentPos.x();
                targetZ = currentPos.z();
            }
        }

        return new BlockPos3i(targetX, targetY, targetZ);
    }

    /**
     * Calculates the offset delta (target - current) required to move the schematic into alignment.
     */
    public static BlockPos3i calculatePlacementOffset(BlockPos3i currentPos, BlockPos3i dimensions, SnapAnchor anchor, boolean snapVerticalSubChunk) {
        BlockPos3i snapped = snapToSubChunk(currentPos, dimensions, anchor, snapVerticalSubChunk);
        return new BlockPos3i(snapped.x() - currentPos.x(), snapped.y() - currentPos.y(), snapped.z() - currentPos.z());
    }

    // =========================================================================
    // 2. FARM PERIMETER & DESPAWN SPHERE CALCULATOR
    // =========================================================================

    /**
     * Calculates the enclosing chunk perimeter for a mob farm and determines the optimal AFK position.
     *
     * @param corner1 First corner of the farm structure
     * @param corner2 Opposite corner of the farm structure
     * @param paddingChunks Additional chunk margin around farm bounding box
     * @param afkHeightAbovePlatform Height offset above farm top platform for AFK spot
     * @return FarmPerimeterResult with bounding coordinates, chunk counts, and AFK coordinates
     */
    public static FarmPerimeterResult calculateFarmPerimeter(BlockPos3i corner1, BlockPos3i corner2, int paddingChunks, int afkHeightAbovePlatform) {
        int minX = Math.min(corner1.x(), corner2.x());
        int maxX = Math.max(corner1.x(), corner2.x());
        int minY = Math.min(corner1.y(), corner2.y());
        int maxY = Math.max(corner1.y(), corner2.y());
        int minZ = Math.min(corner1.z(), corner2.z());
        int maxZ = Math.max(corner1.z(), corner2.z());

        int minChunkX = Math.floorDiv(minX, 16) - Math.max(0, paddingChunks);
        int maxChunkX = Math.floorDiv(maxX, 16) + Math.max(0, paddingChunks);
        int minChunkZ = Math.floorDiv(minZ, 16) - Math.max(0, paddingChunks);
        int maxChunkZ = Math.floorDiv(maxZ, 16) + Math.max(0, paddingChunks);

        BlockPos3i minBoundedBlock = new BlockPos3i(minChunkX * 16, minY, minChunkZ * 16);
        BlockPos3i maxBoundedBlock = new BlockPos3i((maxChunkX * 16) + 15, maxY, (maxChunkZ * 16) + 15);

        int chunkWidth = (maxChunkX - minChunkX) + 1;
        int chunkLength = (maxChunkZ - minChunkZ) + 1;
        int totalChunks = chunkWidth * chunkLength;

        // Optimal horizontal AFK position is at the center of the perimeter bounding box
        int afkX = minBoundedBlock.x() + (maxBoundedBlock.x() - minBoundedBlock.x()) / 2;
        int afkZ = minBoundedBlock.z() + (maxBoundedBlock.z() - minBoundedBlock.z()) / 2;
        // AFK height: default centered or above platform
        int afkY = maxY + afkHeightAbovePlatform;

        BlockPos3i afkPos = new BlockPos3i(afkX, afkY, afkZ);

        // Sub-chunk LC heightmap efficiency calculation:
        // Spawning attempts choose a random Y uniformly between [min_y .. top_non_air_subchunk_max_y].
        // Lowering top sub-chunk increases attempt density inversely proportional to height.
        int subChunkLayerY = Math.floorDiv(minY, 16);
        int totalWorldHeight = 384; // 1.18+ world height: -64 to 320
        int structureHeight = Math.max(1, maxY - minY + 1);
        double efficiencyFactor = (double) totalWorldHeight / (double) Math.max(16, (maxY - (-64) + 1));

        return new FarmPerimeterResult(
            minBoundedBlock,
            maxBoundedBlock,
            new ChunkPos2i(minChunkX, minChunkZ),
            new ChunkPos2i(maxChunkX, maxChunkZ),
            chunkWidth,
            chunkLength,
            totalChunks,
            afkPos,
            128, // Vanilla hostile mob despawn sphere radius
            efficiencyFactor,
            subChunkLayerY
        );
    }

    // =========================================================================
    // 3. NETHER PORTAL LINK GRID (8:1 COORDINATE CONVERTER)
    // =========================================================================

    /**
     * Calculates the bidirectional link coordinates and chunk-boundary safety of a Nether portal.
     *
     * @param portalOrigin Bottom-left corner of portal frame
     * @param frameDimensions Width, height, depth of the portal frame (e.g. 4x5x1)
     * @param isOverworld True if portalOrigin is in Overworld, false if in Nether
     * @return PortalLinkResult with exact target coordinates and chunk boundary analytics
     */
    public static PortalLinkResult calculateNetherPortalLink(BlockPos3i portalOrigin, BlockPos3i frameDimensions, boolean isOverworld) {
        BlockPos3i overworldPos;
        BlockPos3i netherPos;

        if (isOverworld) {
            overworldPos = portalOrigin;
            netherPos = portalOrigin.toNether();
        } else {
            netherPos = portalOrigin;
            overworldPos = portalOrigin.toOverworld();
        }

        // Check if frame spans across chunk boundaries in Overworld
        int owMaxX = overworldPos.x() + Math.max(0, frameDimensions.x() - 1);
        int owMaxZ = overworldPos.z() + Math.max(0, frameDimensions.z() - 1);
        boolean owSingleChunk = (overworldPos.chunkX() == Math.floorDiv(owMaxX, 16)) &&
                                (overworldPos.chunkZ() == Math.floorDiv(owMaxZ, 16));
        int owCrossingBlocks = (owSingleChunk ? 0 : Math.max(Math.floorMod(owMaxX, 16), Math.floorMod(owMaxZ, 16)));

        // Check if frame spans across chunk boundaries in Nether
        int netherMaxX = netherPos.x() + Math.max(0, frameDimensions.x() - 1);
        int netherMaxZ = netherPos.z() + Math.max(0, frameDimensions.z() - 1);
        boolean netherSingleChunk = (netherPos.chunkX() == Math.floorDiv(netherMaxX, 16)) &&
                                    (netherPos.chunkZ() == Math.floorDiv(netherMaxZ, 16));
        int netherCrossingBlocks = (netherSingleChunk ? 0 : Math.max(Math.floorMod(netherMaxX, 16), Math.floorMod(netherMaxZ, 16)));

        // Vanilla portal search bounding box is 128 blocks horizontal in destination dimension
        BlockPos3i netherSearchMin = new BlockPos3i(netherPos.x() - 128, 0, netherPos.z() - 128);
        BlockPos3i netherSearchMax = new BlockPos3i(netherPos.x() + 128, 127, netherPos.z() + 128);

        BlockPos3i owSearchMin = new BlockPos3i(overworldPos.x() - 128, -64, overworldPos.z() - 128);
        BlockPos3i owSearchMax = new BlockPos3i(overworldPos.x() + 128, 319, overworldPos.z() + 128);

        // Verification of bidirectional exact coordinate match:
        // (X_ow / 8) * 8 == X_ow only if X_ow is an exact multiple of 8
        boolean exactMatch = (Math.floorMod(overworldPos.x(), 8) == 0) && (Math.floorMod(overworldPos.z(), 8) == 0);

        return new PortalLinkResult(
            overworldPos,
            netherPos,
            frameDimensions,
            owSingleChunk,
            netherSingleChunk,
            netherSearchMin,
            netherSearchMax,
            owSearchMin,
            owSearchMax,
            exactMatch,
            owCrossingBlocks,
            netherCrossingBlocks
        );
    }

    /**
     * Snaps an Overworld portal position so that both Overworld and Nether portals are exact integer 8-multiples
     * and neither portal frame crosses chunk borders.
     */
    public static BlockPos3i snapPortalToGrid(BlockPos3i currentOverworldPos, BlockPos3i frameDimensions) {
        int targetChunkX = currentOverworldPos.chunkX();
        int targetChunkZ = currentOverworldPos.chunkZ();

        // Overworld chunk bounds: [targetChunkX * 16, targetChunkX * 16 + 15]
        int minChunkX = targetChunkX * 16;
        int minChunkZ = targetChunkZ * 16;

        // Overworld coordinates should be multiples of 8 to avoid rounding drift in Nether (X_ow % 8 == 0)
        // Within a 16-block chunk, the multiples of 8 are relative offset 0 and 8.
        // If portal width is e.g. 4 blocks, starting at offset 0 fits in [0..3], offset 8 fits in [8..11].
        int localX = currentOverworldPos.localX();
        int localZ = currentOverworldPos.localZ();

        int chosenLocalX = (localX < 8) ? 0 : 8;
        int chosenLocalZ = (localZ < 8) ? 0 : 8;

        // Verify if frame fits without crossing boundary (offset + frameWidth <= 16)
        if (chosenLocalX + frameDimensions.x() > 16) {
            chosenLocalX = 0;
        }
        if (chosenLocalZ + frameDimensions.z() > 16) {
            chosenLocalZ = 0;
        }

        return new BlockPos3i(minChunkX + chosenLocalX, currentOverworldPos.y(), minChunkZ + chosenLocalZ);
    }

    // =========================================================================
    // 4. SUB-REGION COORDINATE CALCULATOR & CROSSING MINIMIZER
    // =========================================================================

    public record SubRegionBox(String name, BlockPos3i relativeOffset, BlockPos3i size) {}

    /**
     * Computes chunk crossing analytics for a collection of schematic sub-regions given a placement origin.
     */
    public static List<SubRegionCrossingResult> calculateSubRegionCrossings(BlockPos3i placementOrigin, Collection<SubRegionBox> subRegions) {
        List<SubRegionCrossingResult> results = new ArrayList<>();

        for (SubRegionBox region : subRegions) {
            BlockPos3i worldOrigin = placementOrigin.offset(
                region.relativeOffset().x(),
                region.relativeOffset().y(),
                region.relativeOffset().z()
            );

            int endX = worldOrigin.x() + Math.max(1, region.size().x()) - 1;
            int endZ = worldOrigin.z() + Math.max(1, region.size().z()) - 1;

            int minChunkX = Math.floorDiv(worldOrigin.x(), 16);
            int maxChunkX = Math.floorDiv(endX, 16);
            int minChunkZ = Math.floorDiv(worldOrigin.z(), 16);
            int maxChunkZ = Math.floorDiv(endZ, 16);

            int chunkSpanX = (maxChunkX - minChunkX) + 1;
            int chunkSpanZ = (maxChunkZ - minChunkZ) + 1;
            int totalChunks = chunkSpanX * chunkSpanZ;

            List<ChunkPos2i> intersected = new ArrayList<>(totalChunks);
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    intersected.add(new ChunkPos2i(cx, cz));
                }
            }

            int borderCrossingsX = maxChunkX - minChunkX;
            int borderCrossingsZ = maxChunkZ - minChunkZ;

            results.add(new SubRegionCrossingResult(
                region.name(),
                worldOrigin,
                region.size(),
                chunkSpanX,
                chunkSpanZ,
                totalChunks,
                intersected,
                borderCrossingsX,
                borderCrossingsZ
            ));
        }

        return results;
    }

    /**
     * Searches a 16x16 horizontal search space around current placement origin to find
     * the coordinate offset that minimizes total sub-region chunk boundary crossings.
     */
    public static BlockPos3i findOptimalSubRegionPlacement(BlockPos3i initialOrigin, Collection<SubRegionBox> subRegions) {
        if (subRegions.isEmpty()) {
            return initialOrigin;
        }

        BlockPos3i bestPos = initialOrigin;
        int minTotalCrossings = Integer.MAX_VALUE;

        // Search shifts within [-8 .. +8] on X and Z
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                BlockPos3i candidate = initialOrigin.offset(dx, 0, dz);
                List<SubRegionCrossingResult> analysis = calculateSubRegionCrossings(candidate, subRegions);

                int totalCrossings = 0;
                for (SubRegionCrossingResult res : analysis) {
                    totalCrossings += (res.borderCrossingsX() + res.borderCrossingsZ());
                }

                if (totalCrossings < minTotalCrossings) {
                    minTotalCrossings = totalCrossings;
                    bestPos = candidate;
                } else if (totalCrossings == minTotalCrossings) {
                    // Tie-breaker: choose position closest to initial origin
                    if (candidate.distanceSq(initialOrigin) < bestPos.distanceSq(initialOrigin)) {
                        bestPos = candidate;
                    }
                }
            }
        }

        return bestPos;
    }

    // =========================================================================
    // 5. SLIME CHUNK MATHEMATICAL RNG EVALUATOR
    // =========================================================================

    /**
     * Determines whether a given chunk coordinate is a valid Slime Chunk according to the
     * exact vanilla Minecraft Java Edition LCG RNG algorithm.
     *
     * Formula:
     *   seed = worldSeed + (long)(chunkX * chunkX * 4987142) + (long)(chunkX * 5947611)
     *          + (long)(chunkZ * chunkZ) * 4392871L + (long)(chunkZ * 389711) ^ 987234911L;
     *   return new Random(seed).nextInt(10) == 0;
     *
     * @param worldSeed World seed (64-bit integer)
     * @param chunkX Chunk coordinate X
     * @param chunkZ Chunk coordinate Z
     * @return true if chunk is a slime chunk
     */
    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long x = chunkX;
        long z = chunkZ;

        long term1 = x * x * 4987142L;
        long term2 = x * 5947611L;
        long term3 = z * z * 4392871L;
        long term4 = z * 389711L;

        long combinedSeed = (worldSeed + term1 + term2 + term3 + term4) ^ 987234911L;
        Random rnd = new Random(combinedSeed);
        return rnd.nextInt(10) == 0;
    }

    /**
     * Finds all slime chunks within a given radius around a center chunk.
     */
    public static List<ChunkPos2i> findSlimeChunksInRadius(long worldSeed, int centerChunkX, int centerChunkZ, int radiusChunks) {
        List<ChunkPos2i> result = new ArrayList<>();
        int rad = Math.max(0, radiusChunks);

        for (int cx = centerChunkX - rad; cx <= centerChunkX + rad; cx++) {
            for (int cz = centerChunkZ - rad; cz <= centerChunkZ + rad; cz++) {
                if (isSlimeChunk(worldSeed, cx, cz)) {
                    result.add(new ChunkPos2i(cx, cz));
                }
            }
        }

        // Sort by distance from center
        ChunkPos2i center = new ChunkPos2i(centerChunkX, centerChunkZ);
        result.sort(Comparator.comparingDouble(center::distance));
        return result;
    }

    /**
     * Finds connected clusters of slime chunks (e.g., double, triple, or quad slime chunks)
     * using BFS flood fill.
     */
    public static List<List<ChunkPos2i>> findConnectedSlimeClusters(long worldSeed, int centerChunkX, int centerChunkZ, int radiusChunks, int minClusterSize) {
        List<ChunkPos2i> allSlimeChunks = findSlimeChunksInRadius(worldSeed, centerChunkX, centerChunkZ, radiusChunks);
        Set<ChunkPos2i> unvisited = new HashSet<>(allSlimeChunks);
        List<List<ChunkPos2i>> clusters = new ArrayList<>();

        for (ChunkPos2i chunk : allSlimeChunks) {
            if (!unvisited.contains(chunk)) continue;

            List<ChunkPos2i> cluster = new ArrayList<>();
            Queue<ChunkPos2i> queue = new ArrayDeque<>();
            queue.add(chunk);
            unvisited.remove(chunk);

            while (!queue.isEmpty()) {
                ChunkPos2i curr = queue.poll();
                cluster.add(curr);

                for (ChunkPos2i neighbor : curr.cardinalNeighbors()) {
                    if (unvisited.contains(neighbor)) {
                        unvisited.remove(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            if (cluster.size() >= minClusterSize) {
                cluster.sort(Comparator.comparingInt(ChunkPos2i::x).thenComparingInt(ChunkPos2i::z));
                clusters.add(cluster);
            }
        }

        // Sort clusters by size descending
        clusters.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return clusters;
    }

    /**
     * Checks if a 2x2 chunk block at (chunkX, chunkZ) is a Quad Slime Chunk:
     * [ (x, z), (x+1, z), (x, z+1), (x+1, z+1) ] all slime chunks.
     */
    public static boolean isQuadSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        return isSlimeChunk(worldSeed, chunkX, chunkZ)
            && isSlimeChunk(worldSeed, chunkX + 1, chunkZ)
            && isSlimeChunk(worldSeed, chunkX, chunkZ + 1)
            && isSlimeChunk(worldSeed, chunkX + 1, chunkZ + 1);
    }
}
