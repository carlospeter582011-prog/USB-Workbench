#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/format-io-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/format-io-test-classes tests/native-stubs/android/{content/Context,content/pm/ApplicationInfo,os/Build}.java app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,NativeTools,NativeProtocol,VerifiedWriteBuffer,IoMetrics}.java tests/{DiskToolsTest,FormatIoTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/format-io-test-classes org.usbworkbench.FormatIoTest
