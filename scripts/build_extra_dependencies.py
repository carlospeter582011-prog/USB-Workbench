"""Offline four-ABI libblkid/libuuid + Btrfs build from pinned corresponding sources.
All generated files stay on the Lenovo. Android23 symbols only; no host libraries linked.
"""
from pathlib import Path
import subprocess,os,shutil,re,tarfile,hashlib,json,concurrent.futures
R=Path(__file__).resolve().parents[1]
subprocess.run(['bash','-c','source scripts/check_build_host.sh'],cwd=R,check=True)
HOST={'arm64-v8a':'aarch64-linux-android','armeabi-v7a':'arm-linux-androideabi','x86':'i686-linux-android','x86_64':'x86_64-linux-android'}
SHELL='/bin/bash'
def run(cmd,cwd,env,log):
 with open(R/'artifacts'/log,'a') as f:subprocess.run(cmd,cwd=cwd,env=env,stdout=f,stderr=subprocess.STDOUT,check=True)
def seed(src,dst):
 if dst.exists() or not src.exists():return
 lines=[]
 for line in src.read_text().splitlines():
  if re.match(r'^(ac_cv_|ul_cv_)',line) and not re.match(r'^ac_cv_(env_|build|host|sizeof_|alignof_|type_.*(size|time)|c_bigendian|prog_|sys_largefile|sys_year2038)',line):lines.append(line)
 dst.write_text('\n'.join(lines)+'\n')
def build(abi):
 env=dict(os.environ,USB_NATIVE_ABI=abi,CC=str(R/'tools/native_cc.py'),CXX=str(R/'tools/native_cc.py'),AR='llvm-ar',RANLIB='llvm-ranlib',CFLAGS='-Os -fPIE',LDFLAGS='-pie',CONFIG_SHELL=SHELL)
 base=R/'.downloads/native-build'/abi;util=base/'util-linux';util.mkdir(exist_ok=True);log='0.16-deps-'+abi+'.log'
 seed(R/'.downloads/native-build/arm64-v8a/util-linux/config.cache',util/'config.cache')
 if not (util/'Makefile').exists():run(['bash',str(R/'.downloads/native-src/util-linux-2.41.2/configure'),'-C','--host='+HOST[abi],'--build=x86_64-pc-linux-gnu','--disable-all-programs','--disable-year2038','--enable-libuuid','--enable-libblkid','--disable-shared','--enable-static','--disable-nls','--without-ncurses','--without-python','--without-systemd'],util,env,log)
 if not (util/'usbwb-configured').exists():
  if not (util/'libtool').exists():run(['bash','config.status'],util,env,log)
  (util/'usbwb-configured').write_text('configured\n')
 if not (util/'.libs/libblkid.a').exists():run(['make','-j2','SHELL='+SHELL,'CFLAGS=-Os -fPIE -include '+str(R/'native/util_android.h'),'libuuid.la','libblkid.la'],util,env,log)
 inc=util/'usbwb-include'
 for name,source in [('uuid',R/'.downloads/native-src/util-linux-2.41.2/libuuid/src/uuid.h'),('blkid',util/'libblkid/src/blkid.h')]:
  (inc/name).mkdir(parents=True,exist_ok=True);shutil.copyfile(source,inc/name/(name+'.h'))
 out=base/'btrfs-src'
 if not out.exists():shutil.copytree(R/'.downloads/native-src/btrfs-progs-v6.17',out)
 pc=out/'pkgconfig';pc.mkdir(exist_ok=True)
 for name,libs in [('uuid',str(util/'.libs/libuuid.a')),('blkid',str(util/'.libs/libblkid.a')+' '+str(util/'.libs/libuuid.a')),('zlib','-lz')]:
  (pc/(name+'.pc')).write_text(f'Name: {name}\nDescription: bundled API23 {name}\nVersion: 2.41.2\nLibs: {libs}\nCflags: -I{inc}\n')
 env=dict(env,PKG_CONFIG_LIBDIR=str(pc))
 seed(R/'.downloads/native-build/arm64-v8a/btrfs-src/config.log',out/'config.cache')
 if not (out/'Makefile.inc').exists():run(['bash','configure','-C','--host='+HOST[abi],'--build=x86_64-pc-linux-gnu','--disable-documentation','--disable-convert','--disable-backtrace','--disable-zoned','--disable-zstd','--disable-lzo','--disable-libudev','--disable-python','--with-crypto=builtin'],out,env,log)
 run(['make','-j2','SHELL='+SHELL,'EXTRA_CFLAGS=-I'+str(inc),'mkfs.btrfs','btrfs'],out,env,log)
 dest=R/'app/src/main/jniLibs'/abi
 for name,source in [('mkfs_btrfs','mkfs.btrfs'),('btrfs','btrfs')]:
  shutil.copyfile(out/source,dest/('lib'+name+'.so'));(dest/('lib'+name+'.so')).chmod(0o700);subprocess.run(['llvm-strip','--strip-unneeded',str(dest/('lib'+name+'.so'))],check=True)
 print('Built API23 Btrfs mkfs/checker:',abi,flush=True)
if __name__=='__main__':
 for entry in json.loads((R/'native/sources/extra-engines.json').read_text()):
  if entry['file'] in ['util-linux.tar.xz','btrfs-progs.tar.xz']:assert hashlib.sha256((R/'native/sources'/entry['file']).read_bytes()).hexdigest()==entry['sha256']
 # Source files are cached and pinned in quota.json; never download from this build.
 for filename,folder in [('util-linux.tar.xz','util-linux-2.41.2'),('btrfs-progs.tar.xz','btrfs-progs-v6.17')]:
  if not (R/'.downloads/native-src'/folder).exists():
   with tarfile.open(R/'native/sources'/filename) as t:t.extractall(R/'.downloads/native-src',filter='data')
 abis=os.environ.get('USB_BUILD_ABIS','arm64-v8a armeabi-v7a x86 x86_64').split()
 with concurrent.futures.ThreadPoolExecutor(max_workers=int(os.environ.get("USB_NATIVE_JOBS","1"))) as executor:
  for result in executor.map(build,abis):pass
