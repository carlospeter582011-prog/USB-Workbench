package org.usbworkbench;
import java.io.*;import java.util.*;
/** Read-only structural verification beyond an OEM signature. No mounting required. */
public final class NtfsInspector {
 public static String inspect(BlockDevice volume)throws IOException{
  byte[] boot=new byte[512];NativeProtocol.readBytes(volume,0,boot);
  if(!new String(boot,3,8,java.nio.charset.StandardCharsets.US_ASCII).equals("NTFS    ")||boot[510]!=85||(boot[511]&255)!=170)throw new IOException("NTFS_BOOT_INVALID");
  int sector=OpticalImage.u16(boot,11),spc=boot[13]&255;if(sector!=volume.blockSize()||spc<1||(spc&(spc-1))!=0)throw new IOException("NTFS_BOOT_INVALID");
  long total=OpticalImage.u64(boot,40),mft=OpticalImage.u64(boot,48);if(total<1||total>=volume.blockCount()||mft>=volume.blockCount()/spc)throw new IOException("NTFS_BOOT_INVALID");
  byte[] backup=new byte[512];NativeProtocol.readBytes(volume,total*sector,backup);if(!Arrays.equals(boot,backup))throw new IOException("NTFS_BACKUP_INVALID");
  int encoded=boot[64];long recordSize=encoded<0&&encoded>=-20?1L<<-encoded:encoded>0?(long)encoded*spc*sector:0;
  if(recordSize<sector||recordSize>1048576||recordSize%sector!=0)throw new IOException("NTFS_MFT_INVALID");byte[] record=new byte[(int)recordSize];NativeProtocol.readBytes(volume,mft*spc*sector,record);
  if(record[0]!='F'||record[1]!='I'||record[2]!='L'||record[3]!='E')throw new IOException("NTFS_MFT_INVALID");int usa=OpticalImage.u16(record,4),count=OpticalImage.u16(record,6);if(count!=recordSize/sector+1||usa<8||usa+count*2>recordSize)throw new IOException("NTFS_MFT_INVALID");int sequence=OpticalImage.u16(record,usa);for(int i=1;i<count;i++)if(OpticalImage.u16(record,i*sector-2)!=sequence)throw new IOException("NTFS_MFT_INVALID");
  return "NTFS: primary/backup boot and MFT fixups verified";
 }
}
