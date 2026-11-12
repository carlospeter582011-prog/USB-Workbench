#!/data/data/com.termux/files/usr/bin/python3
"""Exercise bundled ARM64 engines only against disposable sparse files, never /dev."""
import os,pathlib,struct,subprocess,tempfile,threading,json
ROOT=pathlib.Path(__file__).resolve().parents[1]
LIB=ROOT/'app/src/main/jniLibs/arm64-v8a'
def run(tool,args,image,write=True):
 env=dict(os.environ,LD_PRELOAD=str(LIB/'libusbwb_bridge.so'),USBWB_BYTES=str(image.stat().st_size),USBWB_WRITE='1' if write else '0',LC_ALL='C.UTF-8')
 env.pop('LD_LIBRARY_PATH',None)
 env['MKE2FS_CONFIG']=str(ROOT/'artifacts/native-fixtures/mke2fs.conf')
 p=subprocess.Popen([str(LIB/('lib'+tool+'.so')),*args],stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=env)
 timer=threading.Timer(120,p.kill);timer.daemon=True;timer.start()
 logs=[]
 t=threading.Thread(target=lambda:logs.append(p.stderr.read()),daemon=True);t.start()
 count=0
 def exact(n):
  b=b''
  while len(b)<n:
   c=p.stdout.read(n-len(b))
   if not c: raise EOFError('Incomplete protocol')
   b+=c
  return b
 try:
  with image.open('r+b') as disk:
   while True:
    h=p.stdout.read(1)
    if not h:break
    h+=exact(16)
    magic,op,offset,n=struct.unpack('<4sBQI',h)
    assert magic==b'UBW1' and n<=1048576 and offset+n<=image.stat().st_size,(magic,op,offset,n)
    count+=1;disk.seek(offset)
    if op==1:
     payload=disk.read(n);assert len(payload)==n
    elif op==2:
     assert write
     payload=exact(n);disk.write(payload);disk.flush();disk.seek(offset);assert disk.read(n)==payload
     payload=b''
    elif op==3: assert n==0;disk.flush();payload=b''
    else:raise AssertionError(op)
    p.stdin.write(struct.pack('<II',0,n)+payload);p.stdin.flush()
  rc=p.wait(timeout=30);t.join(5)
  log=b''.join(logs).decode(errors='replace')
  print(tool,rc,'RPC calls',count,log[-3000:])
  if rc: raise RuntimeError(tool+' failed')
  return count,log
 finally:
  timer.cancel()
  if p.poll() is None:p.kill()
if __name__=='__main__':
 out=ROOT/'artifacts/native-fixtures';out.mkdir(exist_ok=True)
 (out/'mke2fs.conf').write_text('[defaults]\nbase_features = sparse_super,large_file,filetype,resize_inode,dir_index,ext_attr\nblocksize = 4096\ninode_size = 256\ninode_ratio = 16384\n[fs_types]\next3 = {\nfeatures = has_journal\n}\next4 = {\nfeatures = has_journal,extent,huge_file,flex_bg,metadata_csum,64bit\n}\n')
 ntfs=out/'ntfs.img'
 with ntfs.open('wb') as f:f.truncate(64*1024*1024)
 assert run('mkntfs',['-Q','-F','-s','512','-p','2048','-L','WORKSHOP','/dev/usbworkbench'],ntfs)[0]>0
 run('ntfsinfo',['-f','-m','/dev/usbworkbench'],ntfs,False)
 for fs in ('ext2','ext3','ext4'):
  if not (LIB/'libmke2fs.so').exists():continue
  img=out/(fs+'.img')
  with img.open('wb') as f:f.truncate(64*1024*1024)
  run('mke2fs',['-F','-t',fs,'-b','4096','-m','0','-N','4096','-L','WORKSHOP','-E','nodiscard,lazy_itable_init=0,lazy_journal_init=0',*(['-J','size=4'] if fs!='ext2' else []),'/dev/usbworkbench'],img)
  run('e2fsck',['-f','-n','/dev/usbworkbench'],img,False)
 print('Native file-backed RPC checks passed. No hardware used.')
