#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts/chd-fixtures artifacts/chd-test-classes
python3 - <<'PY'
from pathlib import Path
import struct,hashlib
root=Path('artifacts/chd-fixtures')
for unit in [512,2048,2448]:
 size=unit*5;hunk=unit*2;raw=bytes((i*37+19)&255 for i in range(size));hunks=(size+hunk-1)//hunk
 header=bytearray(124);header[:8]=b'MComprHD';struct.pack_into('>II',header,8,124,5);struct.pack_into('>QQQII',header,32,size,124,0,hunk,unit);header[64:84]=hashlib.sha1(raw).digest();table=b''.join(struct.pack('>I',i+1) for i in range(hunks));data=(header+table).ljust(hunk,b'\0')+raw.ljust(hunks*hunk,b'\0');root.joinpath(str(unit)+'.chd').write_bytes(data);root.joinpath(str(unit)+'.expected.img').write_bytes(raw)
PY
python3 - <<'PYCODEC'
from pathlib import Path
import subprocess,os
root=Path('artifacts/chd-fixtures');env=dict(os.environ);env['LD_LIBRARY_PATH']=str(Path('.downloads/host-tools/lib').resolve())+':/data/data/com.termux/files/usr/lib'
tool=str(Path('.downloads/host-tools/chdman').resolve())
for codec in ['zlib','lzma','zstd','huff']:
 output=root/('compressed-'+codec+'.chd');output.unlink(missing_ok=True)
 command=[tool,'createdvd','-i',str(root/'2048.expected.img'),'-o',str(output),'-hs','4096','-c',codec,'-np','1']
 result=subprocess.run(command,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 if result.returncode:raise RuntimeError(result.stdout.decode(errors='replace'))
 result=subprocess.run([tool,'verify','-i',str(output)],env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 if result.returncode:raise RuntimeError(result.stdout.decode(errors='replace'))
 print('PASS independent chdman created/verified',codec,output.stat().st_size)
PYCODEC
javac -J-XX:ActiveProcessorCount=2 -J-XX:+UseSerialGC -J-Xmx256m -d artifacts/chd-test-classes app/src/main/java/org/usbworkbench/{BlockDevice,DiskTools,ZipTools,OpticalImage,Iso9660Image,UdfImage,FileProtocol,WimFiles,WimProcess,ArchiveFiles,ArchiveProcess,ArchiveTools,WimPaths,UsbAnalyzer,ChdTools,ChdStored}.java tests/{ArchiveTest,ChdTest,ChdStoredTest}.java
java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/chd-test-classes org.usbworkbench.ChdTest

java -XX:ActiveProcessorCount=2 -XX:+UseSerialGC -Xmx256m -cp artifacts/chd-test-classes org.usbworkbench.ChdStoredTest
