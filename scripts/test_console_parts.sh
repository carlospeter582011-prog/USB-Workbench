#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/console-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/console-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,ConsoleParts}.java tests/ConsolePartsTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/console-test-classes org.usbworkbench.ConsolePartsTest
python3 - <<'PY_CHECK'
from pathlib import Path
import struct,hashlib
root=Path('artifacts/console-fixtures');manifest=next(root.glob('*.usbwb')).read_bytes();assert hashlib.sha256(manifest[:-32]).digest()==manifest[-32:];body=manifest[:-32];assert body[:8]==b'UWPS3P01' and body[8]==1
total,part_size,count,name_len=struct.unpack_from('>QQIH',body,9);name=body[31:31+name_len].decode('utf-8');at=31+name_len;data=bytearray()
for i in range(count):
 size=struct.unpack_from('>Q',body,at)[0];digest=body[at+8:at+40];at+=40;part=root.joinpath(name+'.'+str(i)).read_bytes();assert len(part)==size and hashlib.sha256(part).digest()==digest;data+=part
assert len(data)==total and hashlib.sha256(data).digest()==body[at:at+32] and bytes(data)==root.joinpath('original.bin').read_bytes()
print('PASS: independent Python parsed manifest, verified every raw part and rejoined original bytes in numeric order')
PY_CHECK
