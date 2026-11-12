#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/image-test-classes
python3 tools/generate_optical_fixtures.py
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -cp app/libs/xz-1.12.jar -d artifacts/image-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,Fat16,AdvancedDiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,TarTools,VentoyBundle,VentoyInstaller}.java tests/{TarToolsTest,OpticalImageTest,VentoyTest}.java
for test in TarToolsTest OpticalImageTest VentoyTest; do
 java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/image-test-classes:app/libs/xz-1.12.jar "org.usbworkbench.$test" | tee "artifacts/$test.log"
done
