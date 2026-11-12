from pathlib import Path
import hashlib,tarfile,json

def tree_digest(path):
 entries={}
 with tarfile.open(path) as t:
  for m in t:
   name=m.name.rstrip('/')
   assert not name.startswith('/') and '..' not in Path(name).parts and name not in entries
   entries[name]={'mode':m.mode,'type':m.type.decode('ascii'),'link':m.linkname,'sha256':hashlib.sha256(t.extractfile(m).read()).hexdigest() if m.isfile() else None}
 return hashlib.sha256(json.dumps(entries,sort_keys=True,separators=(',',':')).encode()).hexdigest()
def verify(path,entry):
 digest=hashlib.sha256(Path(path).read_bytes()).hexdigest()
 if digest==entry['sha256']:return digest
 # Googlesource regenerates archive container metadata. Exact path/mode/type/link
 # and SHA256 of EVERY file must still equal the pinned original commit tree.
 if entry.get('treeSha256') and tree_digest(path)==entry['treeSha256']:return digest
 raise RuntimeError('Source integrity mismatch: '+str(path))
