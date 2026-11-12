#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/learning-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/learning-test-classes app/src/main/java/org/usbworkbench/{AppLanguage,BlockDevice,DiskTools,ReadMap,UsbAnalyzer,ReportText}.java tests/reference/LegacyReportText.java tests/{AppLanguageTest,ReportSpeedTest,ReadMapTest,ZeroReportTest,IsoPersistenceReportTest}.java
for test in AppLanguageTest ReportSpeedTest ReadMapTest ZeroReportTest IsoPersistenceReportTest;do
 java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/learning-test-classes "org.usbworkbench.$test"
done
node tests/ui.test.cjs
node tests/learning.test.cjs
node tests/es5.test.cjs
