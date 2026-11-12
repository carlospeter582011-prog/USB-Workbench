"""Fast debug-only shell tests; no screenshots. Always restore QA after testing.
python3 tools/device_test_commands.py install
python3 tools/device_test_commands.py probe
python3 tools/device_test_commands.py sample --target-json artifacts/automation-review/usb-probe.json --empty-usb
python3 tools/device_test_commands.py restore
Only sample writes: first4MiB, then original-byte restoration/readback. No format/full-capacity claims.
"""
from pathlib import Path
import subprocess,json,hashlib,shlex,sys,argparse,shutil,time
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'artifacts/automation-review';OUT.mkdir(exist_ok=True)
p=argparse.ArgumentParser(description=__doc__);p.add_argument('op',choices=['install','probe','sample','restore']);p.add_argument('--target-json',type=Path);p.add_argument('--empty-usb',action='store_true');p.add_argument('--detach',action='store_true');a=p.parse_args()
serials=[x.split()[0] for x in subprocess.check_output(['adb','devices'],text=True).splitlines()[1:] if x.endswith('\tdevice')]
if len(serials)!=1:raise SystemExit('Exactly one authorized ADB target required')
serial=serials[0]
def shell(*args):return subprocess.check_output(['adb','-s',serial,'shell',shlex.join(map(str,args))],text=True,stderr=subprocess.STDOUT).strip()
assert shell('getprop','ro.product.model')=='Lenovo YT-J706X'
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def install(path,label):
 assert '(nothing)' in shell('dumpsys','activity','services','org.usbworkbench/.OperationService'),'A real app service is running; stop before swapping builds.'
 sha=digest(path);dst=Path('/storage/emulated/0/Download')/('USBWB-command-tests-'+sha[:12]+'.apk' if label=='debug' else 'USB-Workbench-0.16.1-QA.apk')
 if dst.exists():assert digest(dst)==sha
 else:shutil.copyfile(path,dst)
 stage='/data/local/tmp/usbwb-command-'+sha[:12]+'.apk';shell('cp','/sdcard/Download/'+dst.name,stage);assert shell('sha256sum',stage).split()[0]==sha
 assert shell('pm','install','-r',stage)=='Success';base=shell('pm','path','org.usbworkbench').removeprefix('package:');assert shell('sha256sum',base).split()[0]==sha
 shell('rm',stage)
 if label=='qa':
  assert 'DEBUGGABLE' not in shell('dumpsys','package','org.usbworkbench')
  for own in Path('/storage/emulated/0/Download').glob('USBWB-command-tests-*.apk'):
   # Remove only the exact locally built command-test APK; preserve every other file.
   debug=ROOT/'app/build/outputs/apk/debug/app-debug.apk'
   if debug.exists() and digest(own)==digest(debug):own.unlink()
 receipt={'serial':serial,'build':label,'sha256':sha,'bytes':path.stat().st_size,'priorDataRetained':True,'lanApkPayloadBytes':0}
 (OUT/('command-'+label+'-install.json')).write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt));return
if a.op in ['install','restore']:
 install(ROOT/('app/build/outputs/apk/debug/app-debug.apk' if a.op=='install' else 'artifacts/USB-Workbench-0.16.1-QA.apk'),'debug' if a.op=='install' else 'qa');sys.exit()
extras={'op':a.op,'detach':str(a.detach).lower()}
if a.op=='sample':
 if not a.empty_usb or not a.target_json:p.error('sample requires explicit --empty-usb and exact successful probe receipt')
 assert 0<=time.time()-a.target_json.stat().st_mtime<120, 'Probe receipt expired; probe the connected drive again first.'
 target=json.loads(a.target_json.read_text());assert target['status']=='pass' and target['operation']=='probe'
 extras.update({k:str(target[k]) for k in ['device','vendor','product','blocks','sectorSize','inquiry']});extras['sector']=extras.pop('sectorSize');extras['consent']='EMPTY_TEST_USB'
command=['am','instrument','-w']
for k,v in extras.items():command+=['-e',k,v]
command+=['org.usbworkbench/.CommandTest'];raw=shell(*command);(OUT/('usb-'+a.op+'.log')).write_text(raw+'\n')
line=next((x.removeprefix('INSTRUMENTATION_RESULT: stream=') for x in raw.splitlines() if x.startswith('INSTRUMENTATION_RESULT: stream=')),None)
if line is None:line=next((x for x in raw.splitlines() if x.startswith('{') and x.endswith('}')),None)
if line is None:raise RuntimeError(raw)
result=json.loads(line);(OUT/('usb-'+a.op+'.json')).write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2));sys.exit(0 if result['status']=='pass' else 1)
