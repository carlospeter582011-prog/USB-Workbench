#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
python3 tools/usbextreme_fixtures.py
mkdir -p artifacts/usbextreme-test-classes
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/usbextreme-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,ConsoleParts,UsbExtreme}.java tests/UsbExtremeTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/usbextreme-test-classes org.usbworkbench.UsbExtremeTest
python3 - <<'PY'
from pathlib import Path
import struct
root=Path('artifacts/usbextreme-fixtures');cfg=root.joinpath('ul.cfg').read_bytes();assert len(cfg)==64
name=cfg[:32].split(b'\0')[0];disc=cfg[32:47].split(b'\0')[0];parts,media=struct.unpack_from('BB',cfg,47);assert name==b'Owned Demo' and disc==b'ul.SLUS_123.45' and parts==1 and media==20 and cfg[53]==8
# Independent bitwise non-reflected CRC and full part byte check.
crc=0
for byte in name+b'\0':
 crc^=byte<<24
 for _ in range(8):crc=((crc<<1)^ (0x04c11db7 if crc&0x80000000 else 0))&0xffffffff
part=root/('ul.%08X.SLUS_123.45.00'%crc);assert part.read_bytes()==root.joinpath('owned.iso').read_bytes()
print('PASS independent Python catalog offsets, naming CRC and every ISO byte')
PY
