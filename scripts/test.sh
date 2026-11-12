#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/test-classes artifacts/transport-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/test-classes app/src/main/java/org/usbworkbench/BlockDevice.java app/src/main/java/org/usbworkbench/DiskTools.java tests/DiskToolsTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/test-classes org.usbworkbench.DiskToolsTest | tee artifacts/disk-tests.log
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/transport-classes tests/stubs/android/hardware/usb/*.java app/src/main/java/org/usbworkbench/BlockDevice.java app/src/main/java/org/usbworkbench/UsbDisk.java tests/UsbDiskTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/transport-classes org.usbworkbench.UsbDiskTest | tee artifacts/transport-tests.log
node --check app/src/main/assets/catalog.js
node --check app/src/main/assets/app.js
node tests/ui.test.cjs | tee artifacts/ui-tests.log
python3 tests/payload_audit_test.py
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/test-classes app/src/main/java/org/usbworkbench/BlockDevice.java app/src/main/java/org/usbworkbench/DiskTools.java app/src/main/java/org/usbworkbench/Fat16.java app/src/main/java/org/usbworkbench/AdvancedDiskTools.java app/src/main/java/org/usbworkbench/ZipTools.java tests/DiskToolsTest.java tests/AdvancedToolsTest.java tests/DeepCapacityTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/test-classes org.usbworkbench.AdvancedToolsTest | tee artifacts/advanced-tests.log
node tests/es5.test.cjs | tee artifacts/es5-tests.log
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,NativeProtocol,SliceDevice,WorkshopTools,DiskCloner,IoRegistry}.java tests/{DiskToolsTest,ExpansionTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/test-classes org.usbworkbench.ExpansionTest | tee artifacts/expansion-host-tests.log
for image in artifacts/native-fixtures/fat1*.img; do .downloads/checkers/dosfstools-4.2/src/fsck.fat -n "$image"; done > artifacts/legacy-fat-fsck.log

.downloads/checkers/exfatprogs-1.3.0/fsck/fsck.exfat -n artifacts/native-fixtures/exfat-largecluster.img > artifacts/largecluster-exfat-fsck.log

java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/test-classes org.usbworkbench.DeepCapacityTest
