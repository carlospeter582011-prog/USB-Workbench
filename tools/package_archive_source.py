"""Compact, exact libarchive build source: omit only test trees, never alter compiled code."""
from pathlib import Path
import tarfile,hashlib,io,json
root=Path(__file__).resolve().parents[1];source=root/'native/sources/libarchive.tar.xz';expected='a3dc0872b1b39da34dc18ef1b944f0f11ffd37425d70ae04be679279f1cf43d2'
assert hashlib.sha256(source.read_bytes()).hexdigest()==expected
out=root/'native/sources/libarchive-runtime.tar.xz';files={}
with tarfile.open(source) as src,tarfile.open(out,'w:xz',preset=6) as target:
 for member in src:
  if any(p in {'test','tests','test_utils','.github'} for p in Path(member.name).parts) and not (member.isfile() and member.name.endswith('/test/CMakeLists.txt')):continue
  if not member.name.startswith('libarchive-3.8.11/'):continue
  parts=Path(member.name).parts[1:]
  if len(parts)>1 and parts[0] not in {'libarchive','build','cat','tar','cpio','unzip','libarchive_fe'} and not (parts[0]=='contrib' and parts[1]=='android'):continue
  member.mtime=0;member.uid=member.gid=0;member.uname=member.gname='';member.pax_headers={}
  data=src.extractfile(member).read() if member.isfile() else None
  if data is not None:files[member.name]=hashlib.sha256(data).hexdigest()
  target.addfile(member,io.BytesIO(data) if data is not None else None)
 data=json.dumps({'upstreamArchiveSha256':expected,'policy':'CMake build profile only; tests and unused contrib/docs/examples omitted; compiled sources unchanged','files':files},sort_keys=True,indent=2).encode();entry=tarfile.TarInfo('libarchive-3.8.11/USBWB-PROVENANCE.json');entry.mode=0o644;entry.size=len(data);target.addfile(entry,io.BytesIO(data))
print('Curated libarchive source:',out.stat().st_size,'bytes',len(files),'files SHA256',hashlib.sha256(out.read_bytes()).hexdigest())
# The runner may not contain the optional development dependency graph.
build=root/'.downloads/native-build/arm64-v8a/archive/libarchive'
if build.exists():
 # Verify every libarchive source/header recorded in the real compiler dependency graph is retained.
 import subprocess,re
 build=root/'.downloads/native-build/arm64-v8a/archive/libarchive'
 deps=subprocess.check_output(['ninja','-C',str(build),'-t','deps'],text=True)
 paths=set(re.findall(r'[^\s]*libarchive-3\.8\.11/[^\s]+',deps))
 for path in paths:
  relative='libarchive-3.8.11/'+path.split('libarchive-3.8.11/',1)[1]
  assert relative in files,relative
 print('PASS retained every upstream source/header in actual native build dependencies')
