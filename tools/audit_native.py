"""Validate bundled ELF geometry, ABI, platform dependencies and absence of host paths."""
import json,struct,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ABIS={'arm64-v8a':183,'armeabi-v7a':40,'x86':3,'x86_64':62}
REQUIRED={'libusbwb_bridge.so','libmkntfs.so','libntfsinfo.so','libmke2fs.so','libe2fsck.so','libfile_bridge.so','libwimlib_imagex.so','libmkfs_f2fs.so','libfsck_f2fs.so','libfsck_fat.so','libfsck_exfat.so','libntfsfix.so','libntfsls.so','libntfscat.so','libntfscp.so','libntfsclone.so','libusbwb_archive.so','libusbwb_chd.so','libusbwb_ntfsimage.so','libusbwb_extimage.so'}
REQUIRED.update('lib'+n+'.so' for n in ['mkfs_udf','udfinfo','mkapfs','apfsck','mkfs_hfsplus','fsck_hfsplus','mkfs_btrfs','btrfs','mkfs_xfs','xfs_repair'])
def audit(path,machine):
 path=path.resolve();data=path.read_bytes();assert data[:4]==b'\x7fELF' and data[5]==1,path
 is64=data[4]==2;assert struct.unpack_from('<H',data,18)[0]==machine,path
 phoff=struct.unpack_from('<Q' if is64 else '<I',data,32 if is64 else 28)[0]
 ents,num=struct.unpack_from('<HH',data,54 if is64 else 42)
 loads=[]
 for i in range(num):
  off=phoff+i*ents;kind=struct.unpack_from('<I',data,off)[0]
  if kind==1:
   alignment=struct.unpack_from('<Q' if is64 else '<I',data,off+(48 if is64 else 28))[0];assert alignment>=16384,(path,alignment);loads.append(alignment)
   fileoff=struct.unpack_from('<Q' if is64 else '<I',data,off+(8 if is64 else 4))[0];address=struct.unpack_from('<Q' if is64 else '<I',data,off+(16 if is64 else 8))[0];assert (address-fileoff)%16384==0,path
 assert loads,path
 dynamic=subprocess.check_output(['llvm-readelf','-d',str(path)],text=True)
 assert 'RUNPATH' not in dynamic and 'RPATH' not in dynamic,(path,'host runpath')
 needed=[]
 for line in dynamic.splitlines():
  if '(NEEDED)' in line:
   lib=line.split('[')[1].split(']')[0];assert lib in {'libc.so','libdl.so','libm.so','libz.so','liblog.so'},(path,lib);needed.append(lib)
 return {'file':str(path.relative_to(ROOT)),'bytes':len(data),'loadAlignment':min(loads),'needed':needed}
if __name__=='__main__':
 result=[]
 for abi,machine in ABIS.items():
  paths=list((ROOT/'app/src/main/jniLibs'/abi).glob('*.so'));assert REQUIRED<={p.name for p in paths},abi
  result.extend(audit(p,machine) for p in paths)
 (ROOT/'artifacts/native-audit.json').write_text(json.dumps(result,indent=2)+'\n');print('PASS:',len(result),'native ELF files, 4 ABIs, 16 KiB alignment, system-only dependencies, no host runpaths')
