#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/wim-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/wim-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FileProtocol,WimFiles,WimProcess}.java tests/{WimTest,FileProtocolTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/wim-test-classes org.usbworkbench.FileProtocolTest | tee artifacts/file-rpc-tests.log
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/wim-test-classes org.usbworkbench.WimTest | tee artifacts/wim-tests.log
