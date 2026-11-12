#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/fs-tools-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/fs-tools-test-classes tests/native-stubs/android/{content/Context,content/pm/ApplicationInfo,os/Build}.java app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,SliceDevice,NativeProtocol,NativeTools,VerifiedWriteBuffer,OpticalImage,Iso9660Image,UdfImage,ZipTools,NtfsInspector,F2fsInspector,NativeFormat,ExtraFormat,FilesystemTools}.java tests/{NativeIntegrationTest,FilesystemToolsTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/fs-tools-test-classes org.usbworkbench.FilesystemToolsTest
