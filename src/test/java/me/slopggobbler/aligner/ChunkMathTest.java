package me.slopggobbler.chunkaligner;

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
}
