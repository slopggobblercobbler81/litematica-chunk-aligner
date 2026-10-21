# Litematica Chunk Aligner

A powerful Fabric mod for Minecraft 1.21.4 that provides advanced mathematical precision tools for aligning Litematica schematics to chunk boundaries, optimizing mob farm perimeters, and syncing Nether portal 8:1 coordinates.

---

## Mathematical Foundations & Core Algorithms

### 1. Sub-Chunk 16x16x16 Grid Snapping

Minecraft chunks divide the world into fixed vertical columns of 16×16 blocks horizontal scale, with 16-block vertical sub-chunks ranging from Y = -64 to Y = 319.

When placing schematics, misalignments across chunk and sub-chunk boundaries lead to inefficient block updates, unaligned redstone clock ticks, and broken perimeter alignments.

#### Snap Anchor Formulas:
Given a schematic origin $P = (X, Y, Z)$ and dimensions $D = (W, H, L)$:

- **North-West-Bottom ($NW_B$):**
  $$\text{Chunk}_X = \lfloor X / 16 \rfloor, \quad \text{Chunk}_Z = \lfloor Z / 16 \rfloor$$
  $$X_{\text{target}} = \text{Chunk}_X \times 16, \quad Z_{\text{target}} = \text{Chunk}_Z \times 16$$

- **South-East-Top ($SE_T$):**
  $$X_{\text{target}} = (\text{Chunk}_X \times 16 + 15) - W + 1$$
  $$Z_{\text{target}} = (\text{Chunk}_Z \times 16 + 15) - L + 1$$

- **Center Horizontal ($C_H$):**
  $$\text{Chunks}_X = \lceil W / 16 \rceil, \quad \text{Chunks}_Z = \lceil L / 16 \rceil$$
  $$\text{TotalWidth}_X = \text{Chunks}_X \times 16, \quad \text{TotalLength}_Z = \text{Chunks}_Z \times 16$$
  $$X_{\text{offset}} = (\text{TotalWidth}_X - W) / 2, \quad Z_{\text{offset}} = (\text{TotalLength}_Z - L) / 2$$
  $$X_{\text{target}} = \text{Chunk}_X \times 16 + X_{\text{offset}}, \quad Z_{\text{target}} = \text{Chunk}_Z \times 16 + Z_{\text{offset}}$$

- **Vertical Sub-Chunk Snapping ($Y_{\text{sub}}$):**
  If enabled, the Y coordinate snaps to the base of the 16-block vertical sub-chunk layer:
  $$Y_{\text{sub-index}} = \lfloor Y / 16 \rfloor, \quad Y_{\text{target}} = Y_{\text{sub-index}} \times 16$$

---

### 2. Nether Portal 8:1 Coordinate & Chunk Boundary Alignment Math

In Minecraft, horizontal coordinates between the Overworld and the Nether scale at an exact **8:1 ratio**. Specifically:
$$X_{\text{nether}} = \lfloor X_{\text{overworld}} / 8 \rfloor, \quad Z_{\text{nether}} = \lfloor Z_{\text{overworld}} / 8 \rfloor$$

#### Bidirectional Precision & Rounding Drift:
When converting back from Nether to Overworld:
$$X_{\text{recovered}} = X_{\text{nether}} \times 8, \quad Z_{\text{recovered}} = Z_{\text{nether}} \times 8$$

To prevent rounding drift and portal link fragmentation (where Minecraft searches within a 128-block radius and locks onto unintended portal entities), both Overworld and Nether portal frames must satisfy:
$$\text{mod}(X_{\text{overworld}}, 8) = 0 \quad \text{and} \quad \text{mod}(Z_{\text{overworld}}, 8) = 0$$

#### Chunk-Crossing Safety Check:
A portal frame of size $(W, H)$ placed at $(X, Z)$ must not cross a 16×16 chunk boundary, which causes chunk loading desync during activation.
$$\text{Chunk}_X(X) == \text{Chunk}_X(X + W - 1) \quad \text{and} \quad \text{Chunk}_Z(Z) == \text{Chunk}_Z(Z + L - 1)$$

---

### 3. Farm Perimeter & Mob Despawn Sphere Optimization

For high-efficiency mob farms (e.g., General Mob Farms, Witch Farms, Creepers Farms), entity spawning mechanics dictate that hostile mobs spawn randomly within spherical shells around the player:
- **Immediate Despawn Radius:** $> 128$ blocks Euclidean distance.
- **Immediate Despawn Sphere:** $\Delta X^2 + \Delta Y^2 + \Delta Z^2 > 128^2$.

The mod computes optimal AFK platforms at the precise centroid of the enclosing chunk perimeter bounding box:
$$X_{\text{afk}} = \frac{X_{\min} + X_{\max}}{2}, \quad Z_{\text{afk}} = \frac{Z_{\min} + Z_{\max}}{2}, \quad Y_{\text{afk}} = Y_{\max} + H_{\text{offset}}$$

---

## Features

- **Chunk Grid Snapper HUD & Hotkeys:** Interactive visual overlays showing chunk grid lines and snap preview boxes.
- **Portal Link Calculator:** Real-time 8:1 coordinate converter with chunk boundary violation warnings.
- **Sub-Region Crossings Analyzer:** Detects how many chunks a multi-sub-region Litematica schematic intersects.
- **Farm Perimeter Calculator:** Calculates exact bounding chunks and optimal AFK spots.

## Building from Source

```bash
./gradlew build
```

Artifacts are output to `build/libs/`.


## Configuration & Mod Menu Integration

Litematica Chunk Aligner supports **Mod Menu** and Cloth Config. Access settings via Mod Menu in the title screen.
