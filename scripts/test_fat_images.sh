#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/fat-image-test-classes
python3 tools/generate_fat_fixtures.py
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/fat-image-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FatImage}.java tests/FatImageTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/fat-image-test-classes org.usbworkbench.FatImageTest artifacts/fat-image-fixtures
