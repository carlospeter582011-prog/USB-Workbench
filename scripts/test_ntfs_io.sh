#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/ntfs-io-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/ntfs-io-test-classes tests/native-stubs/android/{content/Context,content/pm/ApplicationInfo,os/Build}.java app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,SliceDevice,NativeProtocol,NativeTools,VerifiedWriteBuffer,IoMetrics,OpticalImage,Iso9660Image,UdfImage,ZipTools,NtfsInspector,F2fsInspector,NativeFormat,ExtraFormat}.java tests/{NativeIntegrationTest,NtfsIoTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/ntfs-io-test-classes org.usbworkbench.NtfsIoTest
