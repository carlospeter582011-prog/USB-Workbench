"""Reuse verified UI tooling; fetch missing pinned packages within the existing quota."""
from pathlib import Path
import base64,hashlib,json,tarfile
from quota_fetch import ROOT,fetch
for name,spec in json.loads((ROOT/'tools/ui-toolchain.json').read_text()).items():
    target=ROOT/'.downloads'/name
    if (target/spec['entry']).is_file():
        print('Reuse',name,spec['version']);continue
    archive=ROOT/'.downloads'/(name+'.tgz')
    if not archive.is_file():fetch(spec['url'],archive,spec['maxBytes'])
    algorithm,want=spec['integrity'].split('-',1)
    assert algorithm=='sha512' and base64.b64encode(hashlib.sha512(archive.read_bytes()).digest()).decode()==want,'Package integrity mismatch'
    target.mkdir(parents=True,exist_ok=True)
    with tarfile.open(archive) as tar:
        for member in tar.getmembers():
            if member.name.startswith('/') or '..' in Path(member.name).parts:raise RuntimeError('Unsafe archive path')
        tar.extractall(target,filter='data')
    print('Verified',name,spec['version'])
