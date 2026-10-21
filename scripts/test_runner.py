#!/usr/bin/env python3
"""
Test Runner Script for Litematica Chunk Aligner
Verifies build and test execution across available Java environments.
"""

import os
import subprocess
import sys

def main():
    workspace = os.path.dirname(os.path.abspath(__file__))
    parent_dir = os.path.dirname(workspace)
    os.chdir(parent_dir)

    print("=== Litematica Chunk Aligner Test Runner ===")
    
    # Check gradle wrapper existence
    gradlew = os.path.join(parent_dir, "gradlew.bat" if os.name == "nt" else "gradlew")
    if not os.path.exists(gradlew):
        print(f"Error: Gradle wrapper not found at {gradlew}")
        sys.exit(1)

    print(f"Running build & tests in {parent_dir}...")
    
    # Run gradle build
    cmd = [gradlew, "build", "--no-daemon"]
    print(f"Executing: {' '.join(cmd)}")
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr)

    if result.returncode != 0:
        print(f"Build failed with exit code {result.returncode}")
        sys.exit(result.returncode)

    print("=== Build and Tests Completed Successfully! ===")

if __name__ == "__main__":
    main()
