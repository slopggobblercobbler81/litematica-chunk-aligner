#!/usr/bin/env python3
"""
CLI Alignment Helper Tool for Litematica Chunk Aligner
Computes chunk coordinates, local offsets, sub-chunk sections, and Nether 8:1 portal links.
"""

import sys

def floor_div(a, b):
    return a // b

def get_chunk_x(x):
    return floor_div(x, 16)

def get_chunk_z(z):
    return floor_div(z, 16)

def get_local_x(x):
    return x % 16

def get_local_z(z):
    return z % 16

def get_sub_chunk_y(y):
    return floor_div(y, 16)

def nether_link(x, y, z, is_overworld=True):
    if is_overworld:
        nx = floor_div(x, 8)
        ny = y
        nz = floor_div(z, 8)
        return {"x": nx, "y": ny, "z": nz}
    else:
        ox = x * 8
        oy = y
        oz = z * 8
        return {"x": ox, "y": oy, "z": oz}

def snap_portal(x, y, z, width=4, length=5):
    cx = get_chunk_x(x)
    cz = get_chunk_z(z)
    min_x = cx * 16
    min_z = cz * 16
    lx = get_local_x(x)
    lz = get_local_z(z)
    
    chosen_lx = 0 if lx < 8 else 8
    chosen_lz = 0 if lz < 8 else 8
    
    if chosen_lx + width > 16:
        chosen_lx = 0
    if chosen_lz + length > 16:
        chosen_lz = 0
        
    return {"x": min_x + chosen_lx, "y": y, "z": min_z + chosen_lz}

def analyze_position(x, y, z):
    return {
        "block": {"x": x, "y": y, "z": z},
        "chunk": {"x": get_chunk_x(x), "z": get_chunk_z(z)},
        "local": {"x": get_local_x(x), "z": get_local_z(z)},
        "subChunkY": get_sub_chunk_y(y),
        "netherLink": nether_link(x, y, z, True),
        "snappedPortal": snap_portal(x, y, z)
    }

def main():
    print("=== Litematica Chunk Aligner CLI Demo ===")
    test_coords = [
        (18, 64, 25),
        (0, 0, 0),
        (-1, -64, -17),
        (160, 120, -80)
    ]
    for x, y, z in test_coords:
        res = analyze_position(x, y, z)
        print(f"Pos {res['block']} -> Chunk: {res['chunk']}, Local: {res['local']}, SubChunkY: {res['subChunkY']}, Nether: {res['netherLink']}, PortalSnap: {res['snappedPortal']}")

if __name__ == "__main__":
    main()
