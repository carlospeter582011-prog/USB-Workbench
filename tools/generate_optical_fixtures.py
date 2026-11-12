"""Independent pycdlib-generated tiny ISO/Joliet/UDF fixtures, no OS payloads."""
from pathlib import Path
from io import BytesIO
import sys,struct,json
root=Path(__file__).resolve().parents[1];sys.path.insert(0,str(root/'.downloads/checkers/pycdlib'));import pycdlib
out=root/'artifacts/optical-fixtures';out.mkdir(exist_ok=True)
def wim():
 b=bytearray(208);b[:8]=b'MSWIM\0\0\0';struct.pack_into('<III',b,8,208,0x10d00,0);b[24:40]=bytes(range(16));struct.pack_into('<HHI',b,40,1,1,1);return bytes(b)
def pe():
 b=bytearray(512);b[:2]=b'MZ';struct.pack_into('<I',b,60,128);b[128:132]=b'PE\0\0';struct.pack_into('<H',b,132,0x8664);struct.pack_into('<H',b,148,240);struct.pack_into('<H',b,152,0x20b);struct.pack_into('<H',b,220,10);return bytes(b)
files=[('/SETUP.EXE;1','/setup.exe',b'setup fixture'),('/SOURCES/BOOT.WIM;1','/sources/boot.wim',wim()),('/SOURCES/INSTALL.ESD;1','/sources/install.esd',wim()),('/EFI/BOOT/BOOTX64.EFI;1','/efi/boot/bootx64.efi',pe()),('/ARABIC.TXT;1','/ملف.txt','مرحبا USB'.encode()),('/EMPTY.;1','/empty',b'')]
for mode in ['plain','joliet','udf']:
 iso=pycdlib.PyCdlib();iso.new(interchange_level=3,joliet=3 if mode=='joliet' else None,udf='2.60' if mode=='udf' else None)
 for upper,lower in [('/SOURCES','/sources'),('/EFI','/efi'),('/EFI/BOOT','/efi/boot')]:
  kw={'joliet_path':lower} if mode=='joliet' else {'udf_path':lower} if mode=='udf' else {};iso.add_directory(upper,**kw)
 expected={}
 for upper,lower,data in files:
  kw={'joliet_path':lower} if mode=='joliet' else {'udf_path':lower} if mode=='udf' else {};iso.add_fp(BytesIO(data),len(data),upper,**kw);expected[(lower if mode!='plain' else upper.split(';')[0].rstrip('.')).lstrip('/')]=data.hex()
 target=out/(mode+'.iso');iso.write(str(target));iso.close();(out/(mode+'.json')).write_text(json.dumps(expected,ensure_ascii=False));print(mode,target.stat().st_size)
