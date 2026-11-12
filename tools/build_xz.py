"""Build only XZ's Java8 baseline, without Java9 multi-release classes or network."""
from pathlib import Path,PurePosixPath
import hashlib,subprocess,zipfile,json
root=Path(__file__).resolve().parents[1];source=root/'third_party/xz-java-1.12.zip';dest=root/'.downloads/xz-java-src';classes=root/'.downloads/xz-java-classes';assert hashlib.sha256(source.read_bytes()).hexdigest()=='d563ba12babaf977f02207c7e474ad41f8ca701efd35ff82abe26c54d66ab68d';dest.mkdir(parents=True,exist_ok=True);classes.mkdir(exist_ok=True)
with zipfile.ZipFile(source) as z:
 for info in z.infolist():
  parts=PurePosixPath(info.filename)
  if parts.is_absolute() or '..' in parts.parts:raise RuntimeError('Unsafe source zip')
  if info.is_dir():continue
  p=dest/info.filename;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(info))
sources=sorted((dest/'src/org/tukaani').rglob('*.java'));args=root/'.downloads/xz-javac.args';args.write_text('\n'.join(json.dumps(str(p)) for p in sources))
subprocess.run(['javac','-J-XX:ActiveProcessorCount=2','-J-XX:+UseSerialGC','-J-Xmx256m','--release','8','-g:none','-encoding','UTF-8','-d',str(classes),'@'+str(args)],check=True)
jar=root/'app/libs/xz-1.12.jar'
with zipfile.ZipFile(jar,'w',compression=zipfile.ZIP_DEFLATED) as z:
 def add(name,data):
  info=zipfile.ZipInfo(name,date_time=(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
 add('META-INF/MANIFEST.MF',b'Manifest-Version: 1.0\nImplementation-Version: 1.12\n\n');add('META-INF/LICENSE-XZ-0BSD.txt',(dest/'COPYING').read_bytes())
 for p in sorted(classes.rglob('*.class')):
  data=p.read_bytes();assert int.from_bytes(data[6:8],'big')==52;add(str(p.relative_to(classes)),data)
(root/'LICENSES/xz-1.12.txt').write_bytes((dest/'COPYING').read_bytes())
print('Built',len(sources),'Java8 source files;',jar.stat().st_size,'bytes; source SHA256',hashlib.sha256(source.read_bytes()).hexdigest())
