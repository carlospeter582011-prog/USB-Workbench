#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/wimtree-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/wimtree-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FileProtocol,WimFiles,WimProcess,ArchiveFiles,ArchiveProcess,ArchiveTools,WimPaths,UsbAnalyzer}.java tests/{WimTest,WimTreeTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/wimtree-test-classes org.usbworkbench.WimTreeTest
