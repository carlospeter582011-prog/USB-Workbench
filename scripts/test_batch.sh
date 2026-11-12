#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/batch-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -cp app/libs/xz-1.12.jar -d artifacts/batch-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,TarTools,VentoyBundle,VentoyInstaller,PartitionConversion,PhoneClone,FileBenchmark,WorkshopTools,ReportText,UsbAnalyzer,ReadMap}.java tests/BatchToolsTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/batch-test-classes:app/libs/xz-1.12.jar org.usbworkbench.BatchToolsTest | tee artifacts/batch-tests.log
