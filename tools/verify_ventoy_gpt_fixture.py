"""Independent standard-library GPT check of the host test's synthetic disk tables."""
from pathlib import Path
import struct,uuid,zlib
root=Path('artifacts/ventoy-gpt-fixture');primary=root.joinpath('primary.bin').read_bytes();backup=root.joinpath('backup.bin').read_bytes()
assert len(primary)==34*512 and len(backup)==33*512
boot=primary[:512];assert boot[92]==0x22 and boot[450]==238 and boot[510:512]==b'\x55\xaa'
a=primary[1024:];b=backup[:16384];assert a==b
heads=[primary[512:1024],backup[-512:]]
for h in heads:
 assert h[:8]==b'EFI PART' and struct.unpack_from('<II',h,8)==(0x10000,92)
 check=bytearray(h[:92]);checksum=struct.unpack_from('<I',check,16)[0];check[16:20]=bytes(4);assert zlib.crc32(check)==checksum
 assert struct.unpack_from('<III',h,80)==(128,128,zlib.crc32(a))
total=struct.unpack_from('<Q',heads[0],32)[0]+1
assert struct.unpack_from('<QQQQ',heads[0],24)==(1,total-1,34,total-34)
assert struct.unpack_from('<QQQQ',heads[1],24)==(total-1,1,34,total-34)
assert struct.unpack_from('<Q',heads[0],72)[0]==2 and struct.unpack_from('<Q',heads[1],72)[0]==total-33
assert heads[0][56:72]==heads[1][56:72]
efi=(total-65536-33)&~7
for offset,start,end,label,attributes in [(0,2048,efi-1,'Ventoy',0),(128,efi,efi+65535,'VTOYEFI',1<<63)]:
 assert a[offset:offset+16]==uuid.UUID('EBD0A0A2-B9E5-4433-87C0-68B6B72699C7').bytes_le
 assert struct.unpack_from('<QQQ',a,offset+32)==(start,end,attributes)
 assert a[offset+56:offset+128].decode('utf-16le').rstrip('\0')==label
assert a[256:]==bytes(len(a)-256)
print('PASS: independent Python primary/backup GPT CRC, geometry, GUIDs, EFI attribute, boot marker and table mirror')
