#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/native-test-classes
python3 tools/native_cc.py -fPIE -pie tests/native_bridge_probe.c -o artifacts/native-bridge-probe
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/native-test-classes tests/native-stubs/android/{content/Context,content/pm/ApplicationInfo,os/Build}.java app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,SliceDevice,NativeProtocol,NativeTools,VerifiedWriteBuffer,OpticalImage,Iso9660Image,UdfImage,ZipTools,NtfsInspector,F2fsInspector,NativeFormat,ExtraFormat,UsbAnalyzer,PersistenceImage,IsoPersistence}.java tests/{ExtraFormatTest,NativeBridgeTest,NativeIntegrationTest,NtfsRegressionTest,PersistenceTest,IsoPersistenceTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.NativeIntegrationTest
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.NtfsRegressionTest
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.PersistenceTest

java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.IsoPersistenceTest

java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.ExtraFormatTest
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/native-test-classes org.usbworkbench.NativeBridgeTest
