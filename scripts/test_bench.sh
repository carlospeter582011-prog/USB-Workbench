#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/bench-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -cp app/libs/xz-1.12.jar -d artifacts/bench-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,Lz4Block,ImageContainers,AdcInputStream,DmgConverter,PartitionMetadata,UsbAnalyzer,ReportText}.java tests/{DiskToolsTest,BenchToolsTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/bench-test-classes:app/libs/xz-1.12.jar org.usbworkbench.BenchToolsTest
