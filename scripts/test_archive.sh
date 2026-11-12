#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/archive-test-classes
python3 tools/archive_fixtures.py
python3 tools/archive_password_fixture.py
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/archive-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FileProtocol,WimFiles,WimProcess,ArchiveFiles,ArchiveProcess,ArchiveTools,WimPaths,UsbAnalyzer}.java tests/ArchiveTest.java tests/ArchivePasswordTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/archive-test-classes org.usbworkbench.ArchiveTest
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/archive-test-classes org.usbworkbench.ArchivePasswordTest
python3 - <<'PY'
from pathlib import Path
import hashlib,json
root=Path('artifacts/archive-fixtures')
for p in root.glob('*.expected.json'):
 expected=json.loads(p.read_text());actual={};out=root/(p.name[:-14]+'.output')
 for n in out.rglob('*'):
  if n.is_file():actual[n.relative_to(out).as_posix()]={'bytes':n.stat().st_size,'sha256':hashlib.sha256(n.read_bytes()).hexdigest()}
 assert actual==expected,(p,actual,expected)
print('PASS independent Python every-file hashes for ZIP/TAR.GZ/7z/RAR fixtures')
PY
