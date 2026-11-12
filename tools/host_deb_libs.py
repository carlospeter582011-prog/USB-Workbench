"""Fetch only missing host-test libraries using cached signed APT metadata; never install or package them."""
from pathlib import Path
import subprocess,re,hashlib,tarfile,io
from quota_fetch import fetch
root=Path(__file__).resolve().parents[1];dest=root/'.downloads/host-tools/lib';dest.mkdir(parents=True,exist_ok=True)
for package in ['sdl2','utf8proc','libxss','libdecor']:
 text=subprocess.check_output(['apt-cache','show',package],text=True).split('\n\n')[0];fields=dict(line.split(': ',1) for line in text.splitlines() if ': ' in line and not line.startswith(' '))
 assert fields['Architecture']=='aarch64';size=int(fields['Size']);assert size<1500000
 policy=subprocess.check_output(['apt-cache','policy',package],text=True);repo=re.search(r'https://[^\s]+',policy).group();p=root/'.downloads'/(package+'-host.deb')
 if not p.exists() or hashlib.sha256(p.read_bytes()).hexdigest()!=fields['SHA256']:fetch(repo+'/'+fields['Filename'],p,size)
 assert hashlib.sha256(p.read_bytes()).hexdigest()==fields['SHA256'];raw=p.read_bytes();at=8;assert raw[:8]==b'!<arch>\n'
 while at<len(raw):
  h=raw[at:at+60];n=int(h[48:58]);name=h[:16].strip().rstrip(b'/');data=raw[at+60:at+60+n];at+=60+n+(n%2)
  if not name.startswith(b'data.tar'):continue
  with tarfile.open(fileobj=io.BytesIO(data)) as t:
   links=[]
   for m in t:
    if '/lib/' not in m.name:continue
    base=Path(m.name).name
    if not re.fullmatch(r'lib[A-Za-z0-9_.+-]+\.so(?:\.[A-Za-z0-9_.+-]+)?',base):continue
    if m.isfile():(dest/base).write_bytes(t.extractfile(m).read())
    elif m.issym():links.append((base,Path(m.linkname).name))
   for base,target in links:
    if (dest/target).is_file() and not (dest/base).exists():(dest/base).symlink_to(target)
 print('Host-only',package,p.stat().st_size,'bytes')
