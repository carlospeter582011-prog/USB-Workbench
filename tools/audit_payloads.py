"""Reject bundled OS images and oversized normal packages; debug APKs are separate."""
from pathlib import Path
import json
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NORMAL_PACKAGE_LIMIT = 70_000_000
IMAGE_SUFFIXES = ('.iso', '.img', '.wim', '.swm', '.esd', '.vhd', '.vhdx', '.vdi', '.qcow', '.qcow2', '.vmdk')


def check_member(name, head):
    lower = name.lower()
    leaf = lower.rsplit('/', 1)[-1]
    if lower.endswith(IMAGE_SUFFIXES):
        raise ValueError('OS/disk image payload prohibited: ' + name)
    if leaf.startswith('ventoy-') and leaf.endswith(('.tar.gz', '.zip', '.tar.xz')):
        raise ValueError('Import Ventoy at runtime; do not bundle: ' + name)
    if head.startswith(b'MSWIM\0\0\0') or head[32769:32774] == b'CD001':
        raise ValueError('Renamed OS image payload prohibited: ' + name)


def audit(path):
    path = Path(path)
    if path.stat().st_size > NORMAL_PACKAGE_LIMIT:
        raise ValueError('Normal package exceeds 70 MB compact-app budget: ' + str(path))
    with zipfile.ZipFile(path) as package:
        for info in package.infolist():
            if info.is_dir():
                continue
            with package.open(info) as stream:
                check_member(info.filename, stream.read(32774))
    return {'file': path.name, 'bytes': path.stat().st_size, 'budgetBytes': NORMAL_PACKAGE_LIMIT,
            'bundledDiskImages': False, 'status': 'PASS'}


if __name__ == '__main__':
    paths = sys.argv[1:] or [ROOT/'app/build/outputs/apk/qa/app-qa.apk',
                            ROOT/'app/build/outputs/bundle/release/app-release.aab']
    results = [audit(path) for path in paths]
    (ROOT/'artifacts/payload-audit.json').write_text(json.dumps(results, indent=2)+'\n')
    print('PASS: no bundled disk/OS images or Ventoy package; normal packages within 70 MB')
