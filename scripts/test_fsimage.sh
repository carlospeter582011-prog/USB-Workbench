#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/fsimage-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/fsimage-test-classes tests/native-stubs/android/{content/Context,content/pm/ApplicationInfo,os/Build}.java app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FileProtocol,WimFiles,WimProcess,ArchiveFiles,ArchiveProcess,ArchiveTools,WimPaths,UsbAnalyzer,SliceDevice,NativeProtocol,NativeTools,VerifiedWriteBuffer,NativeFormat,ExtraFormat,NtfsInspector,F2fsInspector,FilesystemTools,FsImageTools,UsbFileSource}.java tests/{NativeIntegrationTest,ArchiveTest,FsImageTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/fsimage-test-classes org.usbworkbench.FsImageTest
