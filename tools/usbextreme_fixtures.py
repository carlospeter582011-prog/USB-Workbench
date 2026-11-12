"""Owned synthetic SYSTEM.CNF/ISO; no game or loader payload."""
from pathlib import Path
from io import BytesIO
import sys
root=Path(__file__).resolve().parents[1];sys.path.insert(0,str(root/'.downloads/checkers/pycdlib'));import pycdlib
out=root/'artifacts/usbextreme-fixtures';out.mkdir(exist_ok=True)
for name,text in [('owned','BOOT2 = cdrom0:\\SLUS_123.45;1\r\nVER = 1.00\r\n'),('ambiguous','BOOT2 = cdrom0:\\SLUS_123.45;1\nBOOT2=cdrom0:\\SLES_999.99;1\n'),('missing','VER=1.00\n')]:
 iso=pycdlib.PyCdlib();iso.new(interchange_level=3);data=text.encode();iso.add_fp(BytesIO(data),len(data),'/SYSTEM.CNF;1');data=b'Owned synthetic payload only';iso.add_fp(BytesIO(data),len(data),'/OWNED.TXT;1');iso.write(str(out/(name+'.iso')));iso.close()
print('PASS owned pycdlib PS2 metadata fixtures generated')
