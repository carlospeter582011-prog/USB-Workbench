"""Check the built release artifacts, not just source declarations."""
import json,subprocess,zipfile
from audit_native import ABIS,REQUIRED,audit
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
AAPT=Path('/data/data/com.termux/files/usr/opt/android-sdk/build-tools/35.0.0/aapt')
qa=ROOT/'app/build/outputs/apk/qa/app-qa.apk';bundle=ROOT/'app/build/outputs/bundle/release/app-release.aab'
info=subprocess.check_output([str(AAPT),'dump','badging',str(qa)],text=True)
assert "versionCode='20'" in info and "versionName='0.16.1'" in info
assert "targetSdkVersion:'36'" in info and "sdkVersion:'23'" in info
assert 'application-debuggable' not in info
for forbidden in ['android.permission.INTERNET','android.permission.MANAGE_EXTERNAL_STORAGE','android.permission.READ_EXTERNAL_STORAGE','com.google.android.gms.permission.AD_ID']:
 assert forbidden not in info,forbidden
manifest=subprocess.check_output([str(AAPT),'dump','xmltree',str(qa),'AndroidManifest.xml'],text=True)
assert 'instrumentation' not in manifest and 'SmokeTest' not in manifest
with zipfile.ZipFile(qa) as z:
 for abi,machine in ABIS.items():
  assert REQUIRED<={n.rsplit('/',1)[-1] for n in z.namelist() if n.startswith('lib/'+abi+'/')}
  for name in z.namelist():
   if name.startswith('lib/'+abi+'/') and name.endswith('.so'):
    path=ROOT/'app/src/main/jniLibs'/abi/name.rsplit('/',1)[-1];assert z.read(name)==path.read_bytes();audit(path,machine)
 assert 'assets/native-corresponding-source.zip' in z.namelist() and 'assets/ndk-notices.txt' in z.namelist()
 dex=b''.join(z.read(n) for n in z.namelist() if n.endswith('.dex'))
 for method in [b'usbAdvanced',b'extract',b'clearHistory',b'forgetFolder',b'state',b'workshop',b'cloneUsb',b'authorizeClone',b'checksums',b'imageTool',b'officialDownload',b'benchTool',b'convertTable',b'clonePhone',b'convertImage',b'fatImage',b'consoleSplit',b'consoleJoin',b'filesystemTool',b'archiveTool',b'chdTool',b'fsImage',b'fsUsb',b'wimTree',b'usbExtreme',b'initialLanguage',b'readMap',b'partitionMetadata',b'writePersistent']:assert method in dex,method
 for asset in ['assets/index.html','assets/app.js','assets/catalog.js','assets/style.css','assets/compat.js','assets/third-party-notices.txt','assets/native-corresponding-source.zip']:
  assert asset in z.namelist()
  assert z.read(asset)==(ROOT/'app/src/main'/asset).read_bytes(),('packaged asset differs',asset)
 with zipfile.ZipFile(ROOT/'app/src/main/assets/native-corresponding-source.zip') as sources:
  for required in ['native/wim_tree.c','native/sources/libarchive-runtime.tar.xz','native/sources/xz-utils.tar.xz','native/sources/libchdr.tar.gz','native/fsimage_cli.c','scripts/build_archive.sh','scripts/build_chd.sh','scripts/build_fsimage.sh','native/sources/f2fs-tools.tar.gz','scripts/build_f2fs.sh','native/sources/dosfstools.tar.gz','native/sources/exfatprogs.tar.xz','scripts/build_fscheck.sh','native/sources/wimlib.tar.gz','native/wim_compat.h','native/file_bridge.c','scripts/build_wim.sh']:assert required in sources.namelist(),required
with zipfile.ZipFile(bundle) as z:
 assert 'base/manifest/AndroidManifest.xml' in z.namelist() and 'base/dex/classes.dex' in z.namelist()
 assert not any(n.startswith('META-INF/') and n.endswith(('.RSA','.DSA','.EC')) for n in z.namelist()),'Production bundle should remain unsigned until publisher signing'
for lang in ['en','ar']:
 lines=(ROOT/'publishing'/lang/'store-listing.txt').read_text().splitlines()
 assert len(lines[0].split(': ',1)[-1])<=30 and len(lines[1].split(': ',1)[-1])<=80
(ROOT/'artifacts/release-audit.json').write_text(json.dumps({'status':'PASS','targetApi':36,'minApi':23,'qaDebuggable':False,'productionSigning':'unconfigured / unsigned','internetPermission':False,'adIdPermission':False,'nativeLibraries':'4 ABIs; ELF 16 KiB alignment and system dependencies verified','testInstrumentationInQa':False,'bilingualListingLengths':'PASS'},indent=2)+'\n')
print('PASS: release manifest, minification bridge names, AAB structure, unsigned state and listing lengths')
