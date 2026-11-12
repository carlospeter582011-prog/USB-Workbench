#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/container-test-classes artifacts/container-fixtures
python3 - <<'PY'
from pathlib import Path
import random,struct,zlib,gzip,lzma,ctypes
root=Path('artifacts/container-fixtures');data=bytearray(2*1024*1024)
r=random.Random(1943)
for i in range(0,len(data),4096):data[i:i+2048]=r.randbytes(2048)
data[32768:32775]=b'\x01CD001\x01';root.joinpath('original.iso').write_bytes(data)
root.joinpath('python.img.gz').write_bytes(gzip.compress(data));root.joinpath('python.img.xz').write_bytes(lzma.compress(data))
count=len(data)//2048;at=24+4*(count+1);index=[];chunks=[]
for i in range(count):
 raw=bytes(data[i*2048:(i+1)*2048]);co=zlib.compressobj(6,zlib.DEFLATED,-15);packed=co.compress(raw)+co.flush();plain=len(packed)>=len(raw);index.append(at|(0x80000000 if plain else 0));payload=raw if plain else packed;chunks.append(payload);at+=len(payload)
index.append(at);root.joinpath('python.cso').write_bytes(struct.pack('<4sIQIBB2x',b'CISO',24,len(data),2048,1,0)+struct.pack('<'+'I'*len(index),*index)+b''.join(chunks))
lib=ctypes.CDLL('/data/data/com.termux/files/usr/lib/liblz4.so');compress=lib.LZ4_compress_default;compress.argtypes=[ctypes.c_void_p,ctypes.c_void_p,ctypes.c_int,ctypes.c_int];compress.restype=ctypes.c_int
for shift in [0,4]:
 at=(24+4*(count+1)+(1<<shift)-1)&~((1<<shift)-1);index=[];chunks=[bytes(at-(24+4*(count+1)))];start=at
 for i in range(count):
  raw=bytes(data[i*2048:(i+1)*2048]);target=ctypes.create_string_buffer(4096);size=compress(raw,target,len(raw),4096);assert size>0;packed=target.raw[:size];plain=len(packed)>=len(raw);index.append((at>>shift)|(0x80000000 if plain else 0));payload=raw if plain else packed;chunks.append(payload);at+=len(payload)
  if i<count-1:
   padded=(at+(1<<shift)-1)&~((1<<shift)-1);chunks.append(bytes(padded-at));at=padded
 index.append(at>>shift);root.joinpath(f'python-{shift}.zso').write_bytes(struct.pack('<4sIQIBB2x',b'ZISO',24,len(data),2048,1,shift)+struct.pack('<'+'I'*len(index),*index)+b''.join(chunks))
PY
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -cp app/libs/xz-1.12.jar -d artifacts/container-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,ImageContainers,AdcInputStream,DmgConverter,Lz4Block,ReportText,UsbAnalyzer,ReadMap}.java tests/ImageContainersTest.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/container-test-classes:app/libs/xz-1.12.jar org.usbworkbench.ImageContainersTest artifacts/container-fixtures
python3 - <<'PY'
from pathlib import Path
import struct,zlib,gzip
root=Path('artifacts/container-fixtures');expected=root.joinpath('original.iso').read_bytes();data=root.joinpath('java.cso').read_bytes();magic,header,total,block,version,shift=struct.unpack('<4sIQIBB2x',data[:24]);assert (magic,header,total,block,version)==(b'CISO',24,len(expected),2048,1)
n=total//2048;index=struct.unpack('<'+'I'*(n+1),data[24:24+4*(n+1)]);out=bytearray();plain=compressed=0
for i in range(n):
 start=(index[i]&0x7fffffff)<<shift;end=(index[i+1]&0x7fffffff)<<shift
 if index[i]&0x80000000:chunk=data[start:start+2048];plain+=1
 else:chunk=zlib.decompress(data[start:end],-15);compressed+=1
 assert len(chunk)==2048;out+=chunk
assert bytes(out)==expected and plain>0 and compressed>0
assert gzip.decompress(root.joinpath('java.img.gz').read_bytes())==expected
print('PASS: independent Python zlib/GZIP decoder matches every byte; mixed stored/compressed CSO blocks')
PY

python3 - <<'PY'
from pathlib import Path
import ctypes,struct
root=Path('artifacts/container-fixtures');expected=root.joinpath('original.iso').read_bytes();data=root.joinpath('java.zso').read_bytes();magic,header,total,block,version,shift=struct.unpack('<4sIQIBB2x',data[:24]);assert (magic,header,total,block,version)==(b'ZISO',24,len(expected),2048,1)
n=total//2048;index=struct.unpack('<'+'I'*(n+1),data[24:24+4*(n+1)]);out=bytearray();plain=compressed=0
lib=ctypes.CDLL('/data/data/com.termux/files/usr/lib/liblz4.so');decode=lib.LZ4_decompress_safe_partial;decode.argtypes=[ctypes.c_void_p,ctypes.c_void_p,ctypes.c_int,ctypes.c_int,ctypes.c_int];decode.restype=ctypes.c_int
for i in range(n):
 start=(index[i]&0x7fffffff)<<shift;end=(index[i+1]&0x7fffffff)<<shift
 if index[i]&0x80000000:chunk=data[start:start+2048];plain+=1
 else:
  target=ctypes.create_string_buffer(2048);size=decode(data[start:end],target,end-start,2048,2048);assert size==2048;chunk=target.raw;compressed+=1
 out+=chunk
assert bytes(out)==expected and plain>0 and compressed>0
print('PASS: independent installed liblz4 decoded every generated ZSO byte; mixed stored/compressed blocks')
PY
