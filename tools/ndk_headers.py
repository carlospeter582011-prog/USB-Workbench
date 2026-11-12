"""Fetch only NDK headers, API23 stubs and compiler builtins using bounded HTTPS ZIP ranges.
Reuses the tablet's ARM clang; never downloads/runs the NDK's x86 host executables.
"""
import io,json,zipfile
from pathlib import Path
from quota_fetch import ROOT,fetch
URL='https://dl.google.com/android/repository/android-ndk-r30-linux.zip'
SIZE=738633529
CHUNK=1048576
CACHE=ROOT/'.downloads/ndk-ranges';CACHE.mkdir(parents=True,exist_ok=True)
TARGET=ROOT/'.downloads/ndk-sysroot';TARGET.mkdir(parents=True,exist_ok=True)
class RangeFile(io.RawIOBase):
    def __init__(self):self.pos=0
    def seekable(self):return True
    def readable(self):return True
    def tell(self):return self.pos
    def seek(self,offset,whence=0):
        self.pos=offset if whence==0 else self.pos+offset if whence==1 else SIZE+offset
        return self.pos
    def read(self,n=-1):
        if n<0:n=SIZE-self.pos
        n=min(n,SIZE-self.pos)
        if n<=0:return b''
        out=bytearray()
        while len(out)<n:
            part=self.pos//CHUNK;offset=self.pos%CHUNK;end=min(SIZE,(part+1)*CHUNK)-1;p=CACHE/(str(part)+'.bin')
            if not p.exists():fetch(URL+'?usbwb_part='+str(part),p,CHUNK,{'Range':'bytes=%d-%d'%(part*CHUNK,end)},True)
            data=p.read_bytes();take=min(n-len(out),len(data)-offset)
            if take<=0:raise IOError('Invalid ZIP range length')
            out.extend(data[offset:offset+take]);self.pos+=take
        return bytes(out)
records=[]
with zipfile.ZipFile(RangeFile()) as z:
    names=z.namelist();(TARGET/'archive-entries.json').write_text(json.dumps(names))
    prefix='android-ndk-r30/toolchains/llvm/prebuilt/linux-x86_64/'
    for info in z.infolist():
        name=info.filename
        if not name.startswith(prefix) or info.is_dir():continue
        rel=name[len(prefix):]
        selected=rel.startswith('sysroot/usr/include/')
        triples=['aarch64-linux-android','arm-linux-androideabi','i686-linux-android','x86_64-linux-android']
        for triple in triples:
            base='sysroot/usr/lib/'+triple+'/'
            if rel.startswith(base):
                tail=rel[len(base):]
                selected|=tail.startswith('23/') or ('/' not in tail and (tail.endswith('.a') or tail.endswith('.so')))
        selected|=rel.startswith('lib/clang/') and '/lib/linux/' in rel and ('builtins-' in rel or rel.endswith('libunwind.a')) and rel.endswith('.a')
        selected|='NOTICE' in rel and '/' not in rel
        if selected:
            path=TARGET/rel
            if not path.exists():path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(z.read(info))
            records.append({'path':rel,'crc32':info.CRC,'bytes':info.file_size})
(TARGET/'selected-files.json').write_text(json.dumps(records,indent=2))
print('Selected NDK files:',len(records),'extracted bytes:',sum(x['bytes'] for x in records),flush=True)
