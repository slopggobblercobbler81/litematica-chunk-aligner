package me.slopggobbler.aligner;

import me.slopggobblercobbler.chunkaligner.ChunkGridSnapper;
import me.slopggobblercobbler.chunkaligner.math.BlockPos3i;
import me.slopggobblercobbler.chunkaligner.math.PortalLinkResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkMathTest {

    @Test
    public void testNorthWestBottomSnapping() {
        BlockPos3i current = new BlockPos3i(18, 64, 25);
        BlockPos3i dims = new BlockPos3i(10, 10, 10);
        BlockPos3i snapped = ChunkGridSnapper.snapToSubChunk(current, dims, ChunkGridSnapper.SnapAnchor.NORTH_WEST_BOTTOM, false);
        
        assertEquals(16, snapped.x());
        assertEquals(64, snapped.y());
        assertEquals(16, snapped.z());
    }

    @Test
    public void testNetherPortalCoordinateMath() {
        BlockPos3i owPos = new BlockPos3i(160, 64, -80);
        PortalLinkResult result = ChunkGridSnapper.calculateNetherPortalLink(owPos, new BlockPos3i(4, 5, 1), true);

        assertEquals(20, result.netherPos().x());
        assertEquals(64, result.netherPos().y());
        assertEquals(-10, result.netherPos().z());
        assertTrue(result.exactMatch());
    }

    @Test
    public void testPortalGridSnapping() {
        BlockPos3i current = new BlockPos3i(17, 70, 22);
        BlockPos3i snapped = ChunkGridSnapper.snapPortalToGrid(current, new BlockPos3i(4, 5, 1));

        assertEquals(16, snapped.x());
        assertEquals(16, snapped.z());
        assertEquals(0, Math.floorMod(snapped.x(), 8));
        assertEquals(0, Math.floorMod(snapped.z(), 8));
    }

    // =========================================================================
    // Extended Coverage: Zero Boundaries & Negative Coordinates
    // =========================================================================

    @Test
    public void testChunkCoordinateZeroTransitions() {
        // Negative to positive chunk boundary crossing (-1 to 0)
        BlockPos3i negPos = new BlockPos3i(-1, 64, -17);
        assertEquals(-1, negPos.x());
        assertEquals(-1, negPos.chunkX());
        assertEquals(-2, negPos.chunkZ());

        BlockPos3i zeroPos = new BlockPos3i(0, 64, 0);
        assertEquals(0, zeroPos.chunkX());
        assertEquals(0, zeroPos.chunkZ());

        BlockPos3i snappedNeg = ChunkGridSnapper.snapToSubChunk(negPos, new BlockPos3i(5, 5, 5), ChunkGridSnapper.SnapAnchor.NORTH_WEST_BOTTOM, false);
        assertEquals(-16, snappedNeg.x());
        assertEquals(-32, snappedNeg.z());
    }

    // =========================================================================
    // Extended Coverage: 3D Sub-Chunk Alignment Across Build Limits (-64 to 320)
    // =========================================================================

    @Test
    public void testSubChunkVerticalLimits() {
        BlockPos3i bedrockBottom = new BlockPos3i(0, -64, 0);
        assertEquals(-4, bedrockBottom.subChunkY());

        BlockPos3i worldTop = new BlockPos3i(0, 319, 0);
        assertEquals(19, worldTop.subChunkY());

        BlockPos3i snappedBottom = ChunkGridSnapper.snapToSubChunk(bedrockBottom, new BlockPos3i(1, 1, 1), ChunkGridSnapper.SnapAnchor.NORTH_WEST_BOTTOM, true);
        assertEquals(-64, snappedBottom.y());

        BlockPos3i midPos = new BlockPos3i(10, 73, 10);
        BlockPos3i snappedMid = ChunkGridSnapper.snapToSubChunk(midPos, new BlockPos3i(1, 1, 1), ChunkGridSnapper.SnapAnchor.NORTH_WEST_BOTTOM, true);
        assertEquals(64, snappedMid.y()); // sub-chunk Y for 73 is floor(73/16) * 16 = 64
    }

    // =========================================================================
    // Extended Coverage: Nether 8:1 Scaling & Rounding Modes
    // =========================================================================

    @Test
    public void testNether8To1RoundingModes() {
        // Negative coordinates scaling in Nether (-9 / 8 = -2 with floorDiv)
        BlockPos3i negOw = new BlockPos3i(-9, 64, -16);
        PortalLinkResult negResult = ChunkGridSnapper.calculateNetherPortalLink(negOw, new BlockPos3i(4, 5, 1), true);
        assertEquals(-2, negResult.netherPos().x());
        assertEquals(-2, negResult.netherPos().z());
        assertFalse(negResult.exactMatch());

        // Net-to-Overworld roundtrip
        BlockPos3i netherOrigin = new BlockPos3i(10, 64, 10);
        PortalLinkResult owResult = ChunkGridSnapper.calculateNetherPortalLink(netherOrigin, new BlockPos3i(4, 5, 1), false);
        assertEquals(80, owResult.overworldPos().x());
        assertEquals(80, owResult.overworldPos().z());
    }
}
