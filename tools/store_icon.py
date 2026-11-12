"""Render the original USB mark as a 512px PNG without downloads or fonts."""
import math,struct,zlib
from pathlib import Path
W=512;H=512;BG=(11,16,25);FG=(93,228,188);pixels=bytearray(BG*(W*W))
def dot(x,y,coverage):
 if 0<=x<W and 0<=y<H:
  pos=(y*W+x)*3
  for k in range(3):pixels[pos+k]=round(pixels[pos+k]*(1-coverage)+FG[k]*coverage)
def line(ax,ay,bx,by,r=10):
 vx,vy=bx-ax,by-ay;length=vx*vx+vy*vy
 for y in range(max(0,int(min(ay,by)-r-1)),min(H,int(max(ay,by)+r+2))):
  for x in range(max(0,int(min(ax,bx)-r-1)),min(W,int(max(ax,bx)+r+2))):
   t=max(0,min(1,((x-ax)*vx+(y-ay)*vy)/length)) if length else 0
   d=math.hypot(x-(ax+t*vx),y-(ay+t*vy));a=max(0,min(1,r+.5-d))
   if a:dot(x,y,a)
line(256,360,256,140);line(213,183,256,132);line(256,132,299,183);line(256,300,181,255);line(181,255,181,213);line(256,268,331,225);line(331,225,331,181)
for x,y in [(256,365),(181,205),(331,173)]:
 for yy in range(y-20,y+21):
  for xx in range(x-20,x+21):
   if math.hypot(xx-x,yy-y)<20:dot(xx,yy,1)
def chunk(tag,data):return struct.pack('>I',len(data))+tag+data+struct.pack('>I',zlib.crc32(tag+data)&0xffffffff)
raw=b''.join(b'\0'+pixels[y*W*3:(y+1)*W*3] for y in range(W))
Path('publishing/icon-512.png').write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',W,W,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))

# A language-neutral original feature illustration. This is artwork, not a fabricated UI screenshot.
W=1024;H=500;pixels=bytearray(BG*(W*H))
def rectangle(x,y,w,h,color):
 global FG
 old=FG;FG=color
 for yy in range(y,y+h):
  for xx in range(x,x+w):dot(xx,yy,1)
 FG=old
rectangle(48,42,928,416,(17,27,40))
for x in range(72,960,32):
 for y in range(66,448,32):rectangle(x,y,2,2,(37,55,71))
FG=(93,228,188)
line(260,350,260,155,12);line(218,195,260,145,12);line(260,145,302,195,12);line(260,300,186,252,12);line(186,252,186,210,12);line(260,265,334,218,12);line(334,218,334,177,12)
line(260,360,260,360,23)
FG=(87,126,151);line(400,250,450,250,3);line(440,239,451,250,3);line(440,261,451,250,3)
for x,y,color in [(496,94,(142,188,255)),(698,94,(244,197,117)),(596,282,(93,228,188))]:
 rectangle(x,y,166,126,(29,44,61));FG=color
 for a,b,c,d in [(x+45,y+30,x+107,y+30),(x+107,y+30,x+123,y+48),(x+123,y+48,x+123,y+99),(x+123,y+99,x+45,y+99),(x+45,y+99,x+45,y+30)]:line(a,b,c,d,3)
 line(x+59,y+62,x+104,y+62,3);line(x+59,y+77,x+96,y+77,3)
raw=b''.join(b'\0'+pixels[y*W*3:(y+1)*W*3] for y in range(H))
Path('publishing/feature-graphic-1024x500.png').write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',W,H,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
