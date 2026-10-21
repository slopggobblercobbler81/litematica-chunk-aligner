#!/usr/bin/env python3
"""
Unit tests for align_demo.py CLI Alignment Helper Tool.
Verifies positive, zero, and negative coordinate boundaries.
"""

import unittest
import sys
import os

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from align_demo import analyze_position, get_chunk_x, get_sub_chunk_y, nether_link, snap_portal

class TestAlignDemo(unittest.TestCase):

    def test_positive_coordinates(self):
        res = analyze_position(18, 64, 25)
        self.assertEqual(res["chunk"]["x"], 1)
        self.assertEqual(res["chunk"]["z"], 1)
        self.assertEqual(res["local"]["x"], 2)
        self.assertEqual(res["local"]["z"], 9)
        self.assertEqual(res["subChunkY"], 4)
        self.assertEqual(res["netherLink"], {"x": 2, "y": 64, "z": 3})

    def test_zero_coordinates(self):
        res = analyze_position(0, 0, 0)
        self.assertEqual(res["chunk"]["x"], 0)
        self.assertEqual(res["chunk"]["z"], 0)
        self.assertEqual(res["local"]["x"], 0)
        self.assertEqual(res["local"]["z"], 0)
        self.assertEqual(res["subChunkY"], 0)
        self.assertEqual(res["netherLink"], {"x": 0, "y": 0, "z": 0})

    def test_negative_coordinates(self):
        res = analyze_position(-1, -64, -17)
        self.assertEqual(res["chunk"]["x"], -1)
        self.assertEqual(res["chunk"]["z"], -2)
        self.assertEqual(res["local"]["x"], 15)
        self.assertEqual(res["local"]["z"], 15)
        self.assertEqual(res["subChunkY"], -4)
        self.assertEqual(res["netherLink"], {"x": -1, "y": -64, "z": -3})

    def test_portal_snapping(self):
        snapped = snap_portal(17, 70, 22)
        self.assertEqual(snapped["x"] % 8, 0)
        self.assertEqual(snapped["z"] % 8, 0)

if __name__ == "__main__":
    unittest.main()
