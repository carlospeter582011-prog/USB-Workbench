package org.usbworkbench;
import android.content.Context;import java.io.*;import java.util.*;
/** Offline, bounded partition checks and limited upstream repairs; never mounts a filesystem. */
public final class FilesystemTools {
 public static final class Volume {public final SliceDevice disk;public final String kind;public final long start,sectors;Volume(SliceDevice d,String k,long s,long n){disk=d;kind=k;start=s;sectors=n;}}
 public static Volume select(BlockDevice disk,int partition,boolean repair)throws IOException{
  if(disk.blockSize()!=512||disk.blockCount()<4096||disk.blockCount()>0xffffffffL||partition<0||partition>128)throw new IOException("FS_PARTITION");long start=0,count=disk.blockCount();
  if(partition==0){byte[] sector=new byte[512];disk.read(0,sector,512);if(OpticalImage.u16(sector,510)==0xaa55)for(int i=0;i<4;i++){int at=446+i*16;long first=OpticalImage.u32(sector,at+8),n=OpticalImage.u32(sector,at+12);if((sector[at+4]&255)!=0&&first>0&&first<disk.blockCount()&&n>0&&n<=disk.blockCount()-first)throw new IOException("FS_RAW_TABLE");}}
  if(partition>0){byte[] mbr=new byte[512];disk.read(0,mbr,512);if(OpticalImage.u16(mbr,510)!=0xaa55)throw new IOException("FS_PARTITION");List<long[]> areas=new ArrayList<>();boolean found=false;
   if((mbr[450]&255)==238){if(!AdvancedDiskTools.inspectGpt(disk).contains("CRC OK"))throw new IOException("FS_PARTITION");byte[] h=new byte[512];disk.read(1,h,512);if(OpticalImage.u32(h,80)!=128||OpticalImage.u32(h,84)!=128||OpticalImage.u64(h,72)!=2)throw new IOException("FS_PARTITION");byte[] entries=new byte[16384];disk.read(2,entries,entries.length);byte[] basic=AdvancedDiskTools.guid(UUID.fromString("EBD0A0A2-B9E5-4433-87C0-68B6B72699C7")),linux=AdvancedDiskTools.guid(UUID.fromString("0FC63DAF-8483-4772-8E79-3D69D8477DE4"));
    for(int i=0;i<128;i++){int at=i*128;byte[] type=Arrays.copyOfRange(entries,at,at+16);boolean empty=true;for(byte b:type)if(b!=0)empty=false;if(empty)continue;long first=OpticalImage.u64(entries,at+32),last=OpticalImage.u64(entries,at+40);areas.add(new long[]{first,last});if(i+1==partition){if(!Arrays.equals(type,basic)&&!Arrays.equals(type,linux)||repair&&(entries[at+55]&16)!=0)throw new IOException("FS_PARTITION");start=first;count=last-first+1;found=true;}}
   }else{if(partition>4)throw new IOException("FS_PARTITION");for(int i=0;i<4;i++){int at=446+i*16,type=mbr[at+4]&255;long first=OpticalImage.u32(mbr,at+8),n=OpticalImage.u32(mbr,at+12);if(type==0){if(first!=0||n!=0)throw new IOException("FS_PARTITION");continue;}if(!Arrays.asList(1,4,6,7,11,12,14,0x82,0x83,0xef).contains(type)||n<1||first<1||first>=disk.blockCount()||n>disk.blockCount()-first)throw new IOException("FS_PARTITION");areas.add(new long[]{first,first+n-1});if(i+1==partition){if(!Arrays.asList(1,4,6,7,11,12,14,0x83,0xef).contains(type))throw new IOException("FS_UNSUPPORTED");start=first;count=n;found=true;}}}
   if(!found)throw new IOException("FS_PARTITION");Collections.sort(areas,(a,b)->Long.compare(a[0],b[0]));long end=0;for(long[] area:areas){if(area[0]<1||area[0]<=end||area[1]<area[0]||area[1]>=disk.blockCount())throw new IOException("FS_PARTITION");end=area[1];}
  }
  SliceDevice slice=new SliceDevice(disk,start,count);if(count<16)throw new IOException("FS_PARTITION");byte[] bytes=new byte[8192];slice.read(0,bytes,bytes.length);String kind;
  if(new String(bytes,3,8,java.nio.charset.StandardCharsets.US_ASCII).equals("NTFS    ")){if(OpticalImage.u16(bytes,11)!=512)throw new IOException("FS_GEOMETRY");kind="ntfs";}
  else if(new String(bytes,3,8,java.nio.charset.StandardCharsets.US_ASCII).equals("EXFAT   ")){if((bytes[108]&255)!=9)throw new IOException("FS_GEOMETRY");kind="exfat";}
  else if(OpticalImage.u16(bytes,510)==0xaa55&&OpticalImage.u16(bytes,11)==512&&(bytes[13]&255)>0&&((bytes[13]&255)&((bytes[13]&255)-1))==0&&OpticalImage.u16(bytes,14)>0&&(bytes[16]&255)>0&&(bytes[16]&255)<=2&&(OpticalImage.u16(bytes,22)>0||OpticalImage.u32(bytes,36)>0))kind="fat";
  else if(OpticalImage.u16(bytes,1080)==0xef53)kind="ext";
  else if(OpticalImage.u32(bytes,1024)==0xf2f52010L||OpticalImage.u32(bytes,5120)==0xf2f52010L){int sb=OpticalImage.u32(bytes,1024)==0xf2f52010L?1024:5120;if(OpticalImage.u32(bytes,sb+8)!=9||OpticalImage.u32(bytes,sb+16)!=12)throw new IOException("FS_GEOMETRY");kind="f2fs";}
  else throw new IOException("FS_UNSUPPORTED");return new Volume(slice,kind,start,count);
 }
 public static String run(Context c,Volume volume,boolean repair,DiskTools.Progress p,NativeTools.Cancelled cancel,NativeFormat.Stages stages)throws IOException{
  try{return execute(c,volume,repair,p,cancel,stages);}catch(IOException e){if(repair&&e.getMessage()!=null&&(e.getMessage().startsWith("ENGINE_FAILED")||e.getMessage().startsWith("FS_CHECK_ISSUES")||e.getMessage().startsWith("F2FS_VERIFY")))throw new IOException("FS_REPAIR_ISSUES "+volume.kind+"\n"+e.getMessage(),e);if(!repair&&e.getMessage()!=null&&e.getMessage().startsWith("ENGINE_FAILED"))throw new IOException("FS_CHECK_ISSUES "+volume.kind+"\n"+e.getMessage(),e);throw e;}
 }
 private static String execute(Context c,Volume volume,boolean repair,DiskTools.Progress p,NativeTools.Cancelled cancel,NativeFormat.Stages stages)throws IOException{
  String tool=volume.kind.equals("ntfs")?"ntfsfix":volume.kind.equals("ext")?"e2fsck":volume.kind.equals("fat")?"fsck_fat":volume.kind.equals("exfat")?"fsck_exfat":"fsck_f2fs";stages.stage("engine_probe");NativeTools.preflight(c,tool,p,cancel);stages.stage(repair?"fs_repair":"fs_check");
  if(volume.kind.equals("fat")||volume.kind.equals("exfat"))NativeTools.run(c,tool,Arrays.asList(repair?"-p":"-n","/dev/usbworkbench"),volume.disk,repair,p,cancel,repair?1:0);
  else if(volume.kind.equals("ntfs"))NativeTools.run(c,tool,repair?Arrays.asList("/dev/usbworkbench"):Arrays.asList("-n","/dev/usbworkbench"),volume.disk,repair,p,cancel);
  else if(volume.kind.equals("ext")){if(repair)NativeTools.run(c,tool,Arrays.asList("-p","/dev/usbworkbench"),volume.disk,true,p,cancel,1);else NativeTools.run(c,tool,Arrays.asList("-f","-n","/dev/usbworkbench"),volume.disk,false,p,cancel);}
  else NativeTools.run(c,tool,repair?Arrays.asList("-y","--no-kernel-check","/dev/usbworkbench"):Arrays.asList("--dry-run","--no-kernel-check","/dev/usbworkbench"),volume.disk,repair,p,cancel);
  if(repair){stages.stage("filesystem_verify");if(volume.kind.equals("f2fs"))F2fsInspector.inspect(volume.disk);run(c,volume,false,p,cancel,name->{if(!name.equals("engine_probe"))stages.stage(name);});volume.disk.sync();}
  return (repair?"FS_REPAIR_OK ":"FS_CHECK_OK ")+volume.kind;
 }
}
