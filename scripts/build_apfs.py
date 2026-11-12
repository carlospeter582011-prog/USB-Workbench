from pathlib import Path
import os,subprocess,shutil
r=Path('.').resolve();src=next((r/'.downloads/native-src').glob('apfsprogs-*'));o=r/'.downloads/native-build/apfs-patched'
shutil.copytree(src,o,dirs_exist_ok=True)
for folder in ['mkapfs','apfsck']:(o/folder/'version.h').write_text('#define GIT_COMMIT "1bd1c1c2"\n')
# A checker may read-map bounded anonymous buffers; it never writes mapped disk data.
p=o/'apfsck/apfsck.h';s=p.read_text();s=s.replace('#include <unistd.h>','#include <unistd.h>\n#include <errno.h>')
s=s.replace('static inline void *apfs_mmap(','''static inline void *usbwb_readmap(size_t length,int fd,u64 offset){
 if(!length||length>32U*1024*1024||offset>0x7fffffffffffffffULL){errno=EINVAL;return MAP_FAILED;}
 void *memory=mmap(NULL,length,PROT_READ|PROT_WRITE,MAP_PRIVATE|MAP_ANONYMOUS,-1,0);if(memory==MAP_FAILED)return memory;
 size_t at=0;while(at<length){ssize_t n=pread(fd,(char*)memory+at,length-at,(off_t)(offset+at));if(n<0&&errno==EINTR)continue;if(n<=0){munmap(memory,length);errno=EIO;return MAP_FAILED;}at+=n;}return memory;
}
static inline void *apfs_mmap(''',1)
s=s.replace('return mmap(addr, length, prot, flags, fd_tier2, (off_t)offset);','return usbwb_readmap(length,fd_tier2,offset);').replace('return mmap(addr, length, prot, flags, fd_main, (off_t)offset);','return usbwb_readmap(length,fd_main,offset);');p.write_text(s)
p=o/'apfsck/super.c';s=p.read_text();import re
s=re.sub(r'mmap\(NULL, ([^,]+), PROT_READ, MAP_PRIVATE,\s*fd_main, ([^;]+)\);',r'usbwb_readmap(\1, fd_main, \2);',s);p.write_text(s)
for abi in os.environ.get('USB_BUILD_ABIS','arm64-v8a armeabi-v7a x86 x86_64').split():
 env=dict(os.environ,USB_NATIVE_ABI=abi)
 for tool in ['mkapfs','apfsck']:
  sources=list((o/tool).glob('*.c'))+list((o/'lib').glob('*.c'))+list((o/'lib/zlib_inflate').glob('*.c'))
  cmd=['python3','tools/native_cc.py','-fPIE','-pie','-fno-strict-aliasing','-I'+str(o/'include')]+[str(x) for x in sources]+['-o',f'app/src/main/jniLibs/{abi}/lib{tool}.so']
  with open(r/f'artifacts/0.16-{tool}-build.log','w') as f:subprocess.run(cmd,env=env,stdout=f,stderr=subprocess.STDOUT,check=True)
