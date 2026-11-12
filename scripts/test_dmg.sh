#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/dmg-test-classes artifacts/dmg-fixtures
python3 - <<'PY'
from pathlib import Path
import struct,zlib,base64
root=Path('artifacts/dmg-fixtures');raw=bytes(range(256))*4;data=(b'Independent UDIF fixture\n'*100)[:2048].ljust(2048,b'A');expected=raw+data+bytes(1024);packed=zlib.compress(data);fork=raw+packed
block=bytearray(204);struct.pack_into('>4sIQQQ',block,0,b'mish',1,0,8,0);struct.pack_into('>III',block,64,2,32,zlib.crc32(expected));struct.pack_into('>I',block,200,5)
for kind,sector,count,offset,n in [(1,0,2,0,len(raw)),(0x80000005,2,4,len(raw),len(packed)),(0,6,1,0,0),(2,7,1,0,0),(0xffffffff,8,0,0,0)]:block+=struct.pack('>IIQQQQ',kind,0,sector,count,offset,n)
xml=('<?xml version="1.0"?><!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd"><plist version="1.0"><dict><key>resource-fork</key><dict><key>blkx</key><array><dict><key>Name</key><string>Owned &amp; independent</string><key>Data</key><data>\n'+base64.encodebytes(block).decode()+'</data></dict></array></dict></dict></plist>').encode()
f=bytearray(512);struct.pack_into('>4sIII',f,0,b'koly',4,512,0);struct.pack_into('>QQ',f,24,0,len(fork));struct.pack_into('>II',f,56,1,1);struct.pack_into('>QQ',f,216,len(fork),len(xml));struct.pack_into('>Q',f,492,8)
root.joinpath('owned.dmg').write_bytes(fork+xml+f);root.joinpath('expected.img').write_bytes(expected)
PY
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -cp app/libs/xz-1.12.jar -d artifacts/dmg-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,Lz4Block,ImageContainers,AdcInputStream,DmgConverter}.java tests/DmgTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/dmg-test-classes:app/libs/xz-1.12.jar org.usbworkbench.DmgTest
