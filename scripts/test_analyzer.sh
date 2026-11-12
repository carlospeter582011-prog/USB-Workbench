#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/analyzer-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/analyzer-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,UsbAnalyzer}.java tests/UsbAnalyzerTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/analyzer-test-classes org.usbworkbench.UsbAnalyzerTest
