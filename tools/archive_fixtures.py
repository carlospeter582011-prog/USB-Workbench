"""Owned synthetic archives plus small upstream BSD test vectors; never user/OS files."""
from pathlib import Path
import ctypes as c,zipfile,tarfile,io,binascii,json,hashlib
root=Path('artifacts/archive-fixtures');root.mkdir(parents=True,exist_ok=True)
files={'nested/hello.txt':b'Owned archive fixture\n'*200,'nested/ورشة.txt':'ملف اختبار مملوك'.encode(),'empty.bin':b''}
for kind in ['zip','tar.gz','tar.xz']:
 p=root/('owned.'+kind)
 if kind=='zip':
  with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED) as z:
   for n,b in files.items():z.writestr(n,b)
 else:
  with tarfile.open(p,'w:gz' if kind=='tar.gz' else 'w:xz') as z:
   for n,b in files.items():
    entry=tarfile.TarInfo(n);entry.size=len(b);z.addfile(entry,io.BytesIO(b))
L=c.CDLL('/data/data/com.termux/files/usr/lib/libarchive.so')
def fn(name,ret,args):
 f=getattr(L,name);f.restype=ret;f.argtypes=args;return f
new=fn('archive_write_new',c.c_void_p,[]);fmt=fn('archive_write_set_format_7zip',c.c_int,[c.c_void_p]);op=fn('archive_write_open_filename',c.c_int,[c.c_void_p,c.c_char_p]);head=fn('archive_write_header',c.c_int,[c.c_void_p,c.c_void_p]);write=fn('archive_write_data',c.c_ssize_t,[c.c_void_p,c.c_void_p,c.c_size_t]);close=fn('archive_write_close',c.c_int,[c.c_void_p]);free=fn('archive_write_free',c.c_int,[c.c_void_p]);en=fn('archive_entry_new',c.c_void_p,[]);path=fn('archive_entry_set_pathname_utf8',None,[c.c_void_p,c.c_char_p]);size=fn('archive_entry_set_size',None,[c.c_void_p,c.c_int64]);mode=fn('archive_entry_set_mode',None,[c.c_void_p,c.c_uint]);efree=fn('archive_entry_free',None,[c.c_void_p])
a=new();assert fmt(a)==0 and op(a,str(root/'owned.7z').encode())==0
for n,b in files.items():
 e=en();path(e,n.encode());size(e,len(b));mode(e,0o100644);assert head(a,e)==0;assert write(a,b,len(b))==len(b);efree(e)
assert close(a)==0;free(a)
with zipfile.ZipFile(root/'many.zip','w',zipfile.ZIP_DEFLATED) as z:
 for i in range(300):z.writestr('file%03d.txt'%i,('Owned file %d'%i).encode())
with zipfile.ZipFile(root/'unsafe.zip','w') as z:z.writestr('../escape.txt','unsafe')
with zipfile.ZipFile(root/'duplicate.zip','w') as z:z.writestr('Same.txt','one');z.writestr('same.txt','two')
with tarfile.open(root/'link.tar','w') as z:
 e=tarfile.TarInfo('link');e.type=tarfile.SYMTYPE;e.linkname='/outside';z.addfile(e)
for name in ['test_read_format_rar5_stored.rar','test_read_format_rar5_compressed.rar']:
 p=Path('.downloads/native-src/libarchive-3.8.11/libarchive/test')/(name+'.uu')
 if p.exists():
  out=bytearray();started=False
  for line in p.read_bytes().splitlines():
   if line.startswith(b'begin '):started=True;continue
   if not started:continue
   if line==b'end':break
   out.extend(binascii.a2b_uu(line))
  (root/name).write_bytes(out)
# Independent host libarchive decoding produces only expected bytes under generated test directory.
rnew=fn('archive_read_new',c.c_void_p,[]);support=fn('archive_read_support_format_all',c.c_int,[c.c_void_p]);filters=fn('archive_read_support_filter_all',c.c_int,[c.c_void_p]);ropen=fn('archive_read_open_filename',c.c_int,[c.c_void_p,c.c_char_p,c.c_size_t]);nxt=fn('archive_read_next_header',c.c_int,[c.c_void_p,c.POINTER(c.c_void_p)]);pathname=fn('archive_entry_pathname_utf8',c.c_char_p,[c.c_void_p]);ftype=fn('archive_entry_filetype',c.c_uint,[c.c_void_p]);read=fn('archive_read_data',c.c_ssize_t,[c.c_void_p,c.c_void_p,c.c_size_t]);rfree=fn('archive_read_free',c.c_int,[c.c_void_p])
for p in [root/'owned.zip',root/'owned.tar.gz',root/'owned.tar.xz',root/'owned.7z',root/'many.zip']+list(root.glob('test_read_format*.rar')):
 a=rnew();support(a);filters(a);assert ropen(a,str(p).encode(),65536)==0;entries={};e=c.c_void_p();buf=c.create_string_buffer(65536)
 while True:
  code=nxt(a,c.byref(e))
  if code==1:break
  assert code==0,(p,code)
  n=pathname(e);assert n is not None
  name=n.decode();data=bytearray()
  while True:
   count=read(a,buf,len(buf));assert count>=0
   if not count:break
   data.extend(buf.raw[:count])
  if ftype(e)==0o100000:entries[name]={'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()}
 rfree(a);(root/(p.name+'.expected.json')).write_text(json.dumps(entries,ensure_ascii=False))
print('PASS independent host archive fixtures/expected hashes:',len(list(root.glob('*.expected.json'))))
