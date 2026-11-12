from pathlib import Path
import os,subprocess,shutil
r=Path('.').resolve();src=r/'.downloads/native-src/diskdev_cmds-540.1.linux3';o=r/'.downloads/native-build/hfs-patched'
shutil.copytree(src,o,dirs_exist_ok=True)
p=o/'newfs_hfs.tproj/makehfs.c';s=p.read_text();s=s.replace('#include <sys/sysctl.h>','#if !LINUX\n#include <sys/sysctl.h>\n#endif').replace('#include <openssl/sha.h>','')
a=s.index('void GenerateVolumeUUID(VolumeUUID *newVolumeID) {');s=s[:a]+'''/* USB Workbench Android port: use OS entropy instead of weak SHA1 clock/load seeds. */
void GenerateVolumeUUID(VolumeUUID *newVolumeID) {
 int fd=open("/dev/urandom",O_RDONLY);if(fd<0)err(1,"HFS random UUID");
 do {size_t at=0;while(at<sizeof(*newVolumeID)){ssize_t n=read(fd,(char*)newVolumeID+at,sizeof(*newVolumeID)-at);if(n<0&&errno==EINTR)continue;if(n<=0)err(1,"HFS random UUID");at+=n;}}while(!newVolumeID->v.high||!newVolumeID->v.low);
 close(fd);
}
''';p.write_text(s)
for abi in os.environ.get('USB_BUILD_ABIS','arm64-v8a armeabi-v7a x86 x86_64').split():
 env=dict(os.environ,USB_NATIVE_ABI=abi)
 cmd=['python3','tools/native_cc.py','-fPIE','-pie','-include',str(r/'native/hfs_android.h'),'-DLINUX=1','-DBSD=1','-DDEBUG_BUILD=0','-DVERSION="540.1.linux3"','-I'+str(o/'include'),'-I'+str(o/'BlocksRunTime'),'-I'+str(r/'.downloads/native-build'/abi/'ext/lib')]+[str(o/'newfs_hfs.tproj'/x) for x in ['newfs_hfs.c','makehfs.c','hfs_endian.c']]+['-o',f'app/src/main/jniLibs/{abi}/libmkfs_hfsplus.so']
 with open(r/'artifacts/0.16-hfs-build.log','w') as f:subprocess.run(cmd,env=env,stdout=f,stderr=subprocess.STDOUT,check=True)

for p in (o/'fsck_hfs.tproj').rglob('*'):
 if p.suffix in ['.h','.c']:
  s=p.read_text(errors='replace').replace('#include <sys/sysctl.h>','#if !LINUX\n#include <sys/sysctl.h>\n#endif');p.write_text(s)
p=o/'fsck_hfs.tproj/utilities.c';s=p.read_text();marker='#define  DO_VPRINT'
pos=s.index(marker);s=s[:pos]+"static void usbwb_print_str(int type,const char *fmt,const char *str,...){va_list ap;va_start(ap,str);print_to_mem(type,fmt,str,ap);va_end(ap);}"+'\n'+s[pos:]
s=s.replace('print_to_mem(DO_STR,','usbwb_print_str(DO_STR,');p.write_text(s)
def files(folder):
 s=(folder/'Makefile.lnx').read_text().split('OFILES')[0].split('=',1)[1].replace('\\\n',' ')
 return [folder/x for x in s.split() if x.endswith('.c')]

for abi in os.environ.get("USB_BUILD_ABIS","arm64-v8a armeabi-v7a x86 x86_64").split():
 sources=files(o/'fsck_hfs.tproj')+files(o/'fsck_hfs.tproj/dfalib')+files(o/'BlocksRunTime')
 cmd=['python3','tools/native_cc.py','-fPIE','-pie','-fblocks','-DLINUX=1','-DBSD=1','-DDEBUG_BUILD=0','-DVERSION="540.1.linux3"','-include',str(r/'native/hfs_android.h'),'-I'+str(o/'include'),'-I'+str(o/'BlocksRunTime'),'-I'+str(r/'.downloads/native-build'/abi/'ext/lib')]+[str(x) for x in sources]+[str(r/'.downloads/native-build'/abi/'ext/lib/libuuid.a'),'-o',f'app/src/main/jniLibs/{abi}/libfsck_hfsplus.so']
 with open(r/'artifacts/0.16-hfs-checker-build.log','w') as f:subprocess.run(cmd,env=dict(os.environ,USB_NATIVE_ABI=abi),stdout=f,stderr=subprocess.STDOUT,check=True)
