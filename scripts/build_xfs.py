"""Build only API23 XFS formatter and checker, with static userspace dependencies."""
from pathlib import Path
import os,subprocess,shutil,re,hashlib,json,tarfile,concurrent.futures
from build_extra_dependencies import R,HOST,SHELL,run,seed
for entry in json.loads((R/'native/sources/extra-engines.json').read_text()):
 assert hashlib.sha256((R/'native/sources'/entry['file']).read_bytes()).hexdigest()==entry['sha256']
for archive,folder in [('xfsprogs.tar.xz','xfsprogs-6.17.0'),('liburcu.tar.bz2','userspace-rcu-0.15.2'),('inih.tar.gz','inih-r62')]:
 if not (R/'.downloads/native-src'/folder).exists():
  with tarfile.open(R/'native/sources'/archive) as source:source.extractall(R/'.downloads/native-src',filter='data')
def build(abi):
 base=R/'.downloads/native-build'/abi;util=base/'util-linux';urcu=base/'urcu';ini=base/'inih';out=base/'xfs-src';log='0.16-xfs-'+abi+'.log'
 env=dict(os.environ,USB_NATIVE_ABI=abi,CC=str(R/'tools/native_cc.py'),CXX=str(R/'tools/native_cc.py'),AR='llvm-ar',RANLIB='llvm-ranlib',CFLAGS='-Os -fPIE',LDFLAGS='-pie',CONFIG_SHELL=SHELL)
 if not (util/'.libs/libblkid.a').exists():raise RuntimeError('Build modern cached libblkid first: '+abi)
 urcu.mkdir(exist_ok=True);ini.mkdir(exist_ok=True)
 if not (urcu/'Makefile').exists():
  seed(R/'.downloads/native-build/arm64-v8a/urcu/config.log',urcu/'config.cache')
  run(['bash',str(R/'.downloads/native-src/userspace-rcu-0.15.2/configure'),'-C','--host='+HOST[abi],'--build=x86_64-pc-linux-gnu','--disable-shared','--enable-static','--disable-maintainer-mode','--disable-man-pages'],urcu,env,log)
 if not (urcu/'libtool').exists():run(['bash','config.status'],urcu,env,log)
 if not (urcu/'src/.libs/liburcu.a').exists():run(['make','-C',str(urcu/'src'),'-j2','SHELL='+SHELL,'liburcu.la','liburcu-common.la'],urcu,env,log)
 run(['python3',str(R/'tools/native_cc.py'),'-fPIC','-c',str(R/'.downloads/native-src/inih-r62/ini.c'),'-o',str(ini/'ini.o')],R,env,log)
 subprocess.run(['llvm-ar','rcs',str(ini/'libinih.a'),str(ini/'ini.o')],check=True);subprocess.run(['llvm-ar','rcs',str(ini/'libpthread.a')],check=True)
 if not out.exists():shutil.copytree(R/'.downloads/native-src/xfsprogs-6.17.0',out)
 incs=[util/'usbwb-include',R/'.downloads/native-src/inih-r62',urcu/'include',R/'.downloads/native-src/userspace-rcu-0.15.2/include'];incflags=' '.join('-I'+str(x) for x in incs)
 libs='-pie '+' '.join('-L'+str(p) for p in [util/'.libs',urcu/'src/.libs',ini])
 env=dict(env,CPPFLAGS=incflags,LDFLAGS=libs,LIBS='-lurcu-common')
 if not (out/'include/builddefs').exists():
  seed(R/'.downloads/native-build/arm64-v8a/xfs-src/config.cache',out/'config.cache')
  # Do not reuse architecture-dependent atomic64 or C++ detection outcomes.
  cp=out/'config.cache'
  if cp.exists():cp.write_text('\n'.join(x for x in cp.read_text().splitlines() if 'atomic64' not in x and 'cxx' not in x)+'\n')
  run(['bash','configure','-C','--host='+HOST[abi],'--build=x86_64-pc-linux-gnu','--disable-shared','--disable-gettext','--disable-editline','--disable-lib64','--disable-librt','--disable-scrub','--disable-libicu'],out,env,log)
 p=out/'include/builddefs';s=p.read_text();flags='-Os -fPIE '+incflags+' -include '+str(R/'native/xfs_android.h')
 s=re.sub(r'^CFLAGS = .*','CFLAGS = '+flags,s,flags=re.M);s=re.sub(r'^CXXFLAGS = .*','CXXFLAGS = '+flags+' -std=gnu++11',s,flags=re.M)
 # Avoid retaining host debug/optimizer defaults in compact executables.
 s=re.sub(r'^OPTIMIZER\s*=.*','OPTIMIZER = -Os',s,flags=re.M);p.write_text(s)
 run(['make','-j2','SHELL='+SHELL,'headers'],out,env,log)
 # Each target recursively refreshes shared .ltdep files: sequence top-level targets.
 for target in ['libxfs','mkfs','repair']:run(['make','-j2','SHELL='+SHELL,target],out,env,log)
 for name,source in [('mkfs_xfs','mkfs/mkfs.xfs'),('xfs_repair','repair/xfs_repair')]:
  dest=R/'app/src/main/jniLibs'/abi/('lib'+name+'.so');shutil.copyfile(out/source,dest);dest.chmod(0o700);subprocess.run(['llvm-strip','--strip-unneeded',str(dest)],check=True)
 print('Built XFS formatter/read-only checker:',abi,flush=True)
if __name__=='__main__':
 abis=os.environ.get('USB_BUILD_ABIS','arm64-v8a armeabi-v7a x86 x86_64').split()
 with concurrent.futures.ThreadPoolExecutor(max_workers=int(os.environ.get('USB_NATIVE_JOBS','2'))) as executor:
  for result in executor.map(build,abis):pass
