package org.usbworkbench;
import java.io.IOException;
/** Read-only checks of the two formatter-created F2FS superblocks before table commit. */
public final class F2fsInspector {
 public static void inspect(BlockDevice volume)throws IOException{
  if(volume.blockSize()!=512)throw new IOException("FAT_SECTOR");byte[] b=new byte[8192];volume.read(0,b,b.length);int at=1024;
  if(OpticalImage.u32(b,at)!=0xf2f52010L||OpticalImage.u32(b,at+8)!=9||OpticalImage.u32(b,at+12)!=3||OpticalImage.u32(b,at+16)!=12||OpticalImage.u32(b,at+20)!=9)throw new IOException("F2FS_VERIFY");
  for(int i=0;i<3072;i++)if(b[1024+i]!=b[5120+i])throw new IOException("F2FS_VERIFY");long blocks=OpticalImage.u64(b,at+36),segments=OpticalImage.u32(b,at+48),main=OpticalImage.u32(b,at+68);if(blocks<32768||blocks>volume.blockCount()/8||segments<9||main<1||main>segments)throw new IOException("F2FS_VERIFY");
 }
}
