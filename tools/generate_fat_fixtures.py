"""Synthetic FAT images only. No downloaded images or user data."""
from pathlib import Path
import struct, shutil, uuid, zlib
root=Path('artifacts/fat-image-fixtures');root.mkdir(exist_ok=True)
def u16(b,o,v):struct.pack_into('<H',b,o,v)
def u32(b,o,v):struct.pack_into('<I',b,o,v)
def entry(name,attr,cluster,size=0):
 b=bytearray(32);b[:11]=name.encode('ascii');b[11]=attr;u16(b,26,cluster);u32(b,28,size);return b
for bits,total,fat_size,roots,reserved in [(16,32768,128,512,1),(32,140000,1100,0,32)]:
 path=root/f'fat{bits}.img';b=bytearray(512);b[:3]=b'\xeb\x58\x90';b[3:11]=b'USBWB   ';u16(b,11,512);b[13]=1;u16(b,14,reserved);b[16]=2;u16(b,17,roots);b[21]=248;u32(b,32,total);u16(b,510,0xaa55)
 if bits==16:u16(b,22,fat_size);b[54:62]=b'FAT16   '
 else:u32(b,36,fat_size);u32(b,44,2);u16(b,48,1);u16(b,50,6);b[82:90]=b'FAT32   '
 data_sector=reserved+2*fat_size+(roots*32+511)//512
 fat=bytearray(fat_size*512)
 links={0:0xfff8 if bits==16 else 0x0ffffff8,1:0xffff if bits==16 else 0x0fffffff,2:0xffff if bits==16 else 0x0fffffff,3:7,7:0xffff if bits==16 else 0x0fffffff,5:0xffff if bits==16 else 0x0fffffff,6:0xffff if bits==16 else 0x0fffffff}
 for c,n in links.items():(u16 if bits==16 else u32)(fat,c*(bits//8),n)
 file=entry('ARABIC~1TXT',32,3,700);checksum=0
 for n in file[:11]:checksum=(((checksum&1)<<7)+(checksum>>1)+n)&255
 text='ملف عربي.txt';chars=[ord(c) for c in text]+[0];chars += [65535]*(13-len(chars));lfn=bytearray(32);lfn[0]=0x41;lfn[11]=15;lfn[13]=checksum
 for at,c in zip([1,3,5,7,9,14,16,18,20,22,24,28,30],chars):u16(lfn,at,c)
 directory=lfn+file+entry('DIR        ',16,5)+entry('EMPTY   TXT',32,0)+bytearray(32)
 inner=b'Owned nested FAT file\n';payload=bytes((i*7)%256 for i in range(700))
 with path.open('wb') as f:
  f.truncate(total*512);f.seek(0);f.write(b)
  for off in [reserved,reserved+fat_size]:f.seek(off*512);f.write(fat)
  f.seek((reserved+2*fat_size if bits==16 else data_sector)*512);f.write(directory)
  for c,value in [(3,payload[:512]),(7,payload[512:]),(5,entry('.          ',16,5)+entry('..         ',16,0 if bits==16 else 2)+entry('INNER   TXT',32,6,len(inner))+bytearray(32)),(6,inner)]:f.seek((data_sector+c-2)*512);f.write(value)
 # MBR wrapper, no source mutation.
 with (root/f'fat{bits}-mbr.img').open('wb') as out,path.open('rb') as src:
  mbr=bytearray(512);mbr[450]=14 if bits==16 else 12;u32(mbr,454,2048);u32(mbr,458,total);u16(mbr,510,0xaa55);out.write(mbr);out.seek(2048*512);shutil.copyfileobj(src,out,1024*1024)
# GPT wrapping the FAT16 volume using independent Python CRC tables.
volume=root/'fat16.img';total=2048+volume.stat().st_size//512+33;entries=bytearray(16384);entries[:16]=uuid.UUID('EBD0A0A2-B9E5-4433-87C0-68B6B72699C7').bytes_le;entries[16:32]=uuid.uuid4().bytes_le;struct.pack_into('<QQ',entries,32,2048,total-34);guid=uuid.uuid4().bytes_le
with (root/'fat16-gpt.img').open('wb') as out,volume.open('rb') as src:
 out.truncate(total*512);mbr=bytearray(512);mbr[450]=238;u32(mbr,454,1);u32(mbr,458,total-1);u16(mbr,510,0xaa55);out.write(mbr)
 for at,back,where in [(1,total-1,2),(total-1,1,total-33)]:
  h=bytearray(512);h[:8]=b'EFI PART';u32(h,8,0x10000);u32(h,12,92);struct.pack_into('<QQQQ',h,24,at,back,34,total-34);h[56:72]=guid;struct.pack_into('<QIII',h,72,where,128,128,zlib.crc32(entries));u32(h,16,zlib.crc32(h[:92]));out.seek(at*512);out.write(h);out.seek(where*512);out.write(entries)
 out.seek(2048*512);shutil.copyfileobj(src,out,1024*1024)
print('Generated owned FAT16/32 raw/MBR/GPT fixtures')
