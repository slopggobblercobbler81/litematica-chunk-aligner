package me.slopggobblercobbler.chunkaligner;

import me.slopggobblercobbler.chunkaligner.ChunkGridSnapper.SnapAnchor;
import me.slopggobblercobbler.chunkaligner.util.RenderColor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration manager and options holder for Litematica Chunk Aligner.
 * Provides typed settings, snapping modes, slime visualizer parameters, and persistence.
 */
public final class AlignerConfig {

    public enum SnapMode {
        SUB_CHUNK_16("Sub-Chunk 16x16x16"),
        FARM_PERIMETER("Farm Perimeter (Despawn Grid)"),
        NETHER_PORTAL_LINK("Nether Portal 8:1 Link Grid"),
        SUB_REGION_MIN_CROSS("Sub-Region Cross Minimizer");

        private final String displayName;

        SnapMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SnapMode next() {
            SnapMode[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    private static final Path CONFIG_PATH = Paths.get("config", "litematica_chunk_aligner.json");

    // Snapping configuration
    private static boolean enableSnapping = true;
    private static SnapMode snapMode = SnapMode.SUB_CHUNK_16;
    private static SnapAnchor snapAnchor = SnapAnchor.NORTH_WEST_BOTTOM;
    private static boolean snapVerticalSubChunk = true;

    // Slime chunk visualizer configuration
    private static boolean enableSlimeOverlay = true;
    private static long worldSeedOverride = 0L;
    private static boolean useWorldSeedOverride = false;
    private static int overlayRenderDistanceChunks = 8;
    private static int slimeMinY = -64;
    private static int slimeMaxY = 40;
    private static boolean renderFullChunkColumn = false;
    private static float wireframeLineWidth = 2.0f;

    // Portal & Farm configuration
    private static int portalSearchRadiusNether = 128;
    private static boolean enforceSingleChunkPortal = true;
    private static int farmPaddingChunks = 0;
    private static int farmAfkHeightOffset = 128;
    private static boolean renderDespawnSphere = true;

    // Colors
    private static RenderColor slimeFillColor = RenderColor.SLIME_GREEN;
    private static RenderColor slimeBorderColor = RenderColor.SLIME_BORDER;
    private static RenderColor gridColor = RenderColor.GRID_CYAN;
    private static RenderColor portalGridColor = RenderColor.PORTAL_PURPLE;
    private static RenderColor farmPerimeterColor = RenderColor.FARM_ORANGE;
    private static RenderColor despawnSphereColor = RenderColor.DESPAWN_RED;

    private AlignerConfig() {}

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String val = parts[1].trim();

                switch (key) {
                    case "enableSnapping" -> enableSnapping = Boolean.parseBoolean(val);
                    case "snapMode" -> {
                        try { snapMode = SnapMode.valueOf(val); } catch (Exception ignored) {}
                    }
                    case "snapAnchor" -> {
                        try { snapAnchor = SnapAnchor.valueOf(val); } catch (Exception ignored) {}
                    }
                    case "snapVerticalSubChunk" -> snapVerticalSubChunk = Boolean.parseBoolean(val);
                    case "enableSlimeOverlay" -> enableSlimeOverlay = Boolean.parseBoolean(val);
                    case "worldSeedOverride" -> worldSeedOverride = Long.parseLong(val);
                    case "useWorldSeedOverride" -> useWorldSeedOverride = Boolean.parseBoolean(val);
                    case "overlayRenderDistanceChunks" -> overlayRenderDistanceChunks = Integer.parseInt(val);
                    case "slimeMinY" -> slimeMinY = Integer.parseInt(val);
                    case "slimeMaxY" -> slimeMaxY = Integer.parseInt(val);
                    case "renderFullChunkColumn" -> renderFullChunkColumn = Boolean.parseBoolean(val);
                    case "wireframeLineWidth" -> wireframeLineWidth = Float.parseFloat(val);
                    case "portalSearchRadiusNether" -> portalSearchRadiusNether = Integer.parseInt(val);
                    case "enforceSingleChunkPortal" -> enforceSingleChunkPortal = Boolean.parseBoolean(val);
                    case "farmPaddingChunks" -> farmPaddingChunks = Integer.parseInt(val);
                    case "farmAfkHeightOffset" -> farmAfkHeightOffset = Integer.parseInt(val);
                    case "renderDespawnSphere" -> renderDespawnSphere = Boolean.parseBoolean(val);
                }
            }
        } catch (Exception e) {
            System.err.println("[LitematicaChunkAligner] Failed to read config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                writer.write("# Litematica Chunk Aligner Configuration\n");
                writer.write("enableSnapping=" + enableSnapping + "\n");
                writer.write("snapMode=" + snapMode.name() + "\n");
                writer.write("snapAnchor=" + snapAnchor.name() + "\n");
                writer.write("snapVerticalSubChunk=" + snapVerticalSubChunk + "\n");
                writer.write("enableSlimeOverlay=" + enableSlimeOverlay + "\n");
                writer.write("worldSeedOverride=" + worldSeedOverride + "\n");
                writer.write("useWorldSeedOverride=" + useWorldSeedOverride + "\n");
                writer.write("overlayRenderDistanceChunks=" + overlayRenderDistanceChunks + "\n");
                writer.write("slimeMinY=" + slimeMinY + "\n");
                writer.write("slimeMaxY=" + slimeMaxY + "\n");
                writer.write("renderFullChunkColumn=" + renderFullChunkColumn + "\n");
                writer.write("wireframeLineWidth=" + wireframeLineWidth + "\n");
                writer.write("portalSearchRadiusNether=" + portalSearchRadiusNether + "\n");
                writer.write("enforceSingleChunkPortal=" + enforceSingleChunkPortal + "\n");
                writer.write("farmPaddingChunks=" + farmPaddingChunks + "\n");
                writer.write("farmAfkHeightOffset=" + farmAfkHeightOffset + "\n");
                writer.write("renderDespawnSphere=" + renderDespawnSphere + "\n");
            }
        } catch (Exception e) {
            System.err.println("[LitematicaChunkAligner] Failed to write config: " + e.getMessage());
        }
    }

    // Getters and Setters
    public static boolean isEnableSnapping() { return enableSnapping; }
    public static void setEnableSnapping(boolean val) { enableSnapping = val; save(); }

    public static SnapMode getSnapMode() { return snapMode; }
    public static void setSnapMode(SnapMode mode) { snapMode = mode; save(); }

    public static SnapAnchor getSnapAnchor() { return snapAnchor; }
    public static void setSnapAnchor(SnapAnchor anchor) { snapAnchor = anchor; save(); }

    public static boolean isSnapVerticalSubChunk() { return snapVerticalSubChunk; }
    public static void setSnapVerticalSubChunk(boolean val) { snapVerticalSubChunk = val; save(); }

    public static boolean isEnableSlimeOverlay() { return enableSlimeOverlay; }
    public static void setEnableSlimeOverlay(boolean val) { enableSlimeOverlay = val; save(); }

    public static long getWorldSeedOverride() { return worldSeedOverride; }
    public static void setWorldSeedOverride(long seed) {
        worldSeedOverride = seed;
        useWorldSeedOverride = true;
        save();
    }

    public static boolean isUseWorldSeedOverride() { return useWorldSeedOverride; }
    public static void setUseWorldSeedOverride(boolean val) { useWorldSeedOverride = val; save(); }

    public static int getOverlayRenderDistanceChunks() { return overlayRenderDistanceChunks; }
    public static void setOverlayRenderDistanceChunks(int dist) { overlayRenderDistanceChunks = dist; save(); }

    public static int getSlimeMinY() { return slimeMinY; }
    public static void setSlimeMinY(int y) { slimeMinY = y; save(); }

    public static int getSlimeMaxY() { return slimeMaxY; }
    public static void setSlimeMaxY(int y) { slimeMaxY = y; save(); }

    public static boolean isRenderFullChunkColumn() { return renderFullChunkColumn; }
    public static void setRenderFullChunkColumn(boolean val) { renderFullChunkColumn = val; save(); }

    public static float getWireframeLineWidth() { return wireframeLineWidth; }
    public static void setWireframeLineWidth(float w) { wireframeLineWidth = w; save(); }

    public static int getPortalSearchRadiusNether() { return portalSearchRadiusNether; }
    public static void setPortalSearchRadiusNether(int radius) { portalSearchRadiusNether = radius; save(); }

    public static boolean isEnforceSingleChunkPortal() { return enforceSingleChunkPortal; }
    public static void setEnforceSingleChunkPortal(boolean val) { enforceSingleChunkPortal = val; save(); }

    public static int getFarmPaddingChunks() { return farmPaddingChunks; }
    public static void setFarmPaddingChunks(int padding) { farmPaddingChunks = padding; save(); }

    public static int getFarmAfkHeightOffset() { return farmAfkHeightOffset; }
    public static void setFarmAfkHeightOffset(int offset) { farmAfkHeightOffset = offset; save(); }

    public static boolean isRenderDespawnSphere() { return renderDespawnSphere; }
    public static void setRenderDespawnSphere(boolean val) { renderDespawnSphere = val; save(); }

    public static RenderColor getSlimeFillColor() { return slimeFillColor; }
    public static RenderColor getSlimeBorderColor() { return slimeBorderColor; }
    public static RenderColor getGridColor() { return gridColor; }
    public static RenderColor getPortalGridColor() { return portalGridColor; }
    public static RenderColor getFarmPerimeterColor() { return farmPerimeterColor; }
    public static RenderColor getDespawnSphereColor() { return despawnSphereColor; }
}
