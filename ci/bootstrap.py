"""Fetch verified upstream dependencies on GitHub; never needed on tablet uploads."""
from pathlib import Path
import json,hashlib,urllib.request,tarfile,base64,subprocess,os,zipfile,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];os.chdir(R)
def fetch(url,dst,digest=None,algorithm='sha256'):
 dst.parent.mkdir(parents=True,exist_ok=True)
 if not dst.exists():
  print('Fetching',url,flush=True)
  subprocess.run(['curl','--fail','--location','--retry','3','--silent','--show-error',url,'-o',str(dst)],check=True)
 if digest:
  got=hashlib.new(algorithm,dst.read_bytes()).hexdigest();assert got==digest,('Digest mismatch',dst,got)
 return dst
(R/'artifacts').mkdir(exist_ok=True);(R/'.downloads/native-src').mkdir(parents=True,exist_ok=True)
for entry in json.loads((R/'ci/dependencies.json').read_text()):
 for url in [entry['url'],*entry.get('mirrors',[])]:
  try:path=fetch(url,R/entry['file']);break
  except subprocess.CalledProcessError:
   (R/entry['file']).unlink(missing_ok=True)
   if url==[entry['url'],*entry.get('mirrors',[])][-1]:raise
 from source_integrity import verify
 verify(path,entry)
 if path.parent.name=='sources' and path.name!='f2fs-tools.tar.gz':
  with tarfile.open(path) as t:t.extractall(R/'.downloads/native-src',filter='data')
for name,spec in json.loads((R/'tools/ui-toolchain.json').read_text()).items():
 p=fetch(spec['url'],R/'.downloads'/(name+'.tgz'))
 assert base64.b64encode(hashlib.sha512(p.read_bytes()).digest()).decode()==spec['integrity'].split('-',1)[1]
 with tarfile.open(p) as t:t.extractall(R/'.downloads'/name,filter='data')
if '--native' in __import__('sys').argv:
 ndk=R/'.downloads/android-ndk-r30'
 if not (ndk/'source.properties').exists():
  meta=fetch('https://dl.google.com/android/repository/repository2-1.xml',R/'.downloads/android-repository.xml')
  packages=ET.parse(meta).getroot()
  match=None
  for package in packages:
   if not package.tag.endswith('remotePackage') or not package.get('path','').startswith('ndk;'):continue
   for complete in package.iter('complete'):
    url=complete.findtext('url')
    if url=='android-ndk-r30-linux.zip':match=(complete.findtext('checksum'),complete.findtext('size'))
  assert match,'Pinned NDK r30 not listed in official Google SDK repository'
  archive=fetch('https://dl.google.com/android/repository/android-ndk-r30-linux.zip',R/'.downloads/ndk-r30.zip',match[0],'sha1')
  assert archive.stat().st_size==int(match[1])
  subprocess.run(['unzip','-q',str(archive),'-d',str(R/'.downloads')],check=True)
  # zipfile does not restore executable bits.
  for p in (ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin').iterdir():
   if p.is_file():p.chmod(p.stat().st_mode|0o111)
  archive.unlink()
 toolchain=ndk/'toolchains/llvm/prebuilt/linux-x86_64'
 target=R/'.downloads/ndk-sysroot';target.mkdir(exist_ok=True)
 if not (target/'sysroot').exists():(target/'sysroot').symlink_to(toolchain/'sysroot',target_is_directory=True)
 for abi in ['arm64-v8a','armeabi-v7a','x86','x86_64']:
  (R/'.downloads/native-build'/abi).mkdir(parents=True,exist_ok=True);(R/'app/src/main/jniLibs'/abi).mkdir(parents=True,exist_ok=True)
 with Path(os.environ['GITHUB_ENV']).open('a') as f:f.write('USB_NDK_ROOT='+str(ndk)+'\nUSB_LLVM_BIN='+str(toolchain/'bin')+'\n')
 with Path(os.environ['GITHUB_PATH']).open('a') as f:f.write(str(toolchain/'bin')+'\n')
for p in [R/'tools/native_cc.py',R/'gradlew']:p.chmod(0o755)
