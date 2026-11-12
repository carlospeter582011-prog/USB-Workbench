import struct,binascii,random,zipfile
from pathlib import Path
# Owned ZIP/ZipCrypto encoder; stdlib zipfile independently decrypts it below.
def make(password,data):
 keys=[0x12345678,0x23456789,0x34567890]
 table=[]
 for n in range(256):
  for _ in range(8):n=(n>>1)^ (0xedb88320 if n&1 else 0)
  table.append(n)
 def update(x):
  keys[0]=(keys[0]>>8)^table[(keys[0]^x)&255];keys[1]=((keys[1]+(keys[0]&255))*134775813+1)&0xffffffff;keys[2]=(keys[2]>>8)^table[(keys[2]^(keys[1]>>24))&255]
 for x in password:update(x)
 def encrypt(raw):
  out=bytearray()
  for x in raw:
   t=keys[2]|2;out.append(x^(((t*(t^1))>>8)&255));update(x)
  return bytes(out)
 crc=binascii.crc32(data)&0xffffffff;name=b'owned.txt';payload=encrypt(bytes(range(11))+bytes([crc>>24]))+encrypt(data)
 local=struct.pack('<I5H3I2H',0x04034b50,20,1,0,0,0,crc,len(payload),len(data),len(name),0)+name+payload
 central=struct.pack('<I6H3I5H2I',0x02014b50,20,20,1,0,0,0,crc,len(payload),len(data),len(name),0,0,0,0,0,0)+name
 return local+central+struct.pack('<I4H2IH',0x06054b50,0,0,1,1,len(central),len(local),0)
p=Path('artifacts/archive-fixtures/password.zip');p.parent.mkdir(exist_ok=True);p.write_bytes(make(b'fixture-only',b'Owned encrypted test data\n'*50))
with zipfile.ZipFile(p) as z:assert z.read('owned.txt',pwd=b'fixture-only')==b'Owned encrypted test data\n'*50
print('Independent Python ZipCrypto decryption PASS')
