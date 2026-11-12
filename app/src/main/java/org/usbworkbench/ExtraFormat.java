package org.usbworkbench;
import android.content.Context;
import java.io.*;import java.util.*;
/** Extra userspace formatters; all writes are bounded to one partition, table committed last. */
public final class ExtraFormat {
 public static boolean supports(String fs){return Arrays.asList("udf","apfs","hfsplus","btrfs","xfs").contains(fs);}
 public static void format(Context c,BlockDevice disk,String fs,String table,String label,int cluster,DiskTools.Progress p,NativeTools.Cancelled cancel,NativeFormat.Stages stages)throws IOException{
  if(!supports(fs)||!Arrays.asList("mbr","gpt").contains(table)||cluster!=0)throw new IOException("FORMAT_OPTIONS");
  if(disk.blockSize()!=512)throw new IOException("FAT_SECTOR");
  if(fs.equals("apfs")&&!table.equals("gpt"))throw new IOException("EXTRA_FORMAT_GPT");
  if((fs.equals("apfs")||fs.equals("btrfs"))&&disk.blockCount()<524288)throw new IOException("EXTRA_FORMAT_SIZE");
  if(fs.equals("xfs")&&disk.blockCount()<1048576)throw new IOException("EXTRA_FORMAT_SIZE");
  if(disk.blockCount()<67584||disk.blockCount()>67108864L)throw new IOException("EXTRA_FORMAT_SIZE");
  if((fs.equals("xfs")&&label.length()>12)||label.trim().isEmpty()||!label.matches("[A-Za-z0-9 _-]{1,15}"))throw new IOException("EXTRA_FORMAT_LABEL");for(char ch:label.toCharArray())if(ch<32||ch>126)throw new IOException("EXTRA_FORMAT_LABEL");
  String tool=fs.equals("udf")?"mkfs_udf":fs.equals("apfs")?"mkapfs":fs.equals("btrfs")?"mkfs_btrfs":fs.equals("xfs")?"mkfs_xfs":"mkfs_hfsplus",checker=fs.equals("udf")?"udfinfo":fs.equals("apfs")?"apfsck":fs.equals("btrfs")?"btrfs":fs.equals("xfs")?"xfs_repair":"fsck_hfsplus";
  if(!NativeTools.available(c,tool)||!NativeTools.available(c,checker))throw new IOException("ENGINE_MISSING");
  stages.stage("engine_probe");NativeTools.preflight(c,tool,p,cancel);NativeTools.preflight(c,checker,p,cancel);if(cancel.get())throw new IOException("CANCELLED");
  boolean gpt=table.equals("gpt");long count=disk.blockCount()-2048-(gpt?33:0);if(!fs.equals("udf"))count=count/8*8;SliceDevice volume=new SliceDevice(disk,2048,count);
  stages.stage("partition_prepare");byte[] zero=new byte[16384];for(long at=0;at<2048;){if(cancel.get())throw new IOException("CANCELLED");int n=(int)Math.min(32,2048-at);disk.write(at,zero,n*512);at+=n;p.update(at,2048);}for(long at=disk.blockCount()-33;at<disk.blockCount();){if(cancel.get())throw new IOException("CANCELLED");int n=(int)Math.min(32,disk.blockCount()-at);disk.write(at,zero,n*512);at+=n;}
  stages.stage(fs+"_metadata");
  if(fs.equals("udf"))NativeTools.run(c,tool,Arrays.asList("--utf8","--blocksize=512","--media-type=hd","--udfrev=1.02","--bootarea=erase","--label="+label,"/dev/usbworkbench"),volume,true,p,cancel);
  else if(fs.equals("apfs"))NativeTools.run(c,tool,Arrays.asList("-L",label,"-U",UUID.randomUUID().toString(),"-u",UUID.randomUUID().toString(),"/dev/usbworkbench"),volume,true,p,cancel);
  else if(fs.equals("btrfs"))NativeTools.run(c,tool,Arrays.asList("-f","-K","-L",label,"-m","single","-d","single","-n","16384","-s","4096","--csum","crc32c","/dev/usbworkbench"),volume,true,p,cancel);
  else if(fs.equals("xfs"))NativeTools.run(c,tool,Arrays.asList("-f","-K","-b","size=4096","-s","size=512","-m","crc=1,finobt=1,rmapbt=0,reflink=0,bigtime=0,inobtcount=0","-L",label,"/dev/usbworkbench"),volume,true,p,cancel);
  else NativeTools.run(c,tool,Arrays.asList("-v",label,"/dev/usbworkbench"),volume,true,p,cancel);
  volume.sync();stages.stage("filesystem_verify");
  if(fs.equals("udf")){verifyUdf(volume);String proof=NativeTools.run(c,checker,Arrays.asList("--utf8","--blocksize=512","/dev/usbworkbench"),volume,false,p,cancel);if(!proof.contains("udfrev=1.02")||!proof.contains("label="+label+"\n"))throw new IOException("VERIFY_FAILED");}
  else if(fs.equals("apfs")){verifyApfs(volume);NativeTools.run(c,checker,Collections.singletonList("/dev/usbworkbench"),volume,false,p,cancel);}
  else if(fs.equals("btrfs")){verifyBtrfs(volume);NativeTools.run(c,checker,Arrays.asList("check","--readonly","/dev/usbworkbench"),volume,false,p,cancel);}
  else if(fs.equals("xfs")){verifyXfs(volume);NativeTools.run(c,checker,Arrays.asList("-n","-m","128","/dev/usbworkbench"),volume,false,p,cancel);}
  else {verifyHfs(volume);NativeTools.run(c,checker,Arrays.asList("-f","-n","/dev/usbworkbench"),volume,false,p,cancel);}
  if(cancel.get())throw new IOException("CANCELLED");stages.stage("partition_commit");p.update(0,1);
  if(gpt)AdvancedDiskTools.installGpt(disk,2048,2048+count-1,label,fs.equals("apfs")?"7C3457EF-0000-11AA-AA11-00306543ECAC":(fs.equals("btrfs")||fs.equals("xfs"))?"0FC63DAF-8483-4772-8E79-3D69D8477DE4":fs.equals("hfsplus")?"48465300-0000-11AA-AA11-00306543ECAC":"EBD0A0A2-B9E5-4433-87C0-68B6B72699C7");
  else{byte[] mbr=new byte[512];mbr[450]=(byte)(fs.equals("hfsplus")?0xaf:(fs.equals("btrfs")||fs.equals("xfs"))?0x83:7);Arrays.fill(mbr,447,450,(byte)0xff);Arrays.fill(mbr,451,454,(byte)0xff);DiskTools.le32(mbr,454,2048);DiskTools.le32(mbr,458,count);mbr[510]=0x55;mbr[511]=(byte)0xaa;disk.write(0,mbr,512);byte[] b=new byte[512];disk.read(0,b,512);if(!Arrays.equals(mbr,b))throw new IOException("VERIFY_FAILED");}
  disk.sync();if(gpt)AdvancedDiskTools.inspectGpt(disk);p.update(1,1);
 }
 static void verifyXfs(BlockDevice volume)throws IOException{byte[] b=new byte[512];volume.read(0,b,512);if(!new String(b,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("XFSB")||be32(b,4)!=4096||be64(b,8)<1||be64(b,8)>volume.blockCount()/8||(((b[100]&255)<<8|(b[101]&255))&15)!=5||((b[102]&255)<<8|(b[103]&255))!=512||be32(b,84)<1||be32(b,88)<1)throw new IOException("VERIFY_FAILED");long expected=u32(b,224);Arrays.fill(b,224,228,(byte)0);int crc=-1;for(byte v:b){crc^=v&255;for(int j=0;j<8;j++)crc=(crc>>>1)^((crc&1)!=0?0x82f63b78:0);}if(expected!=((~crc)&0xffffffffL))throw new IOException("VERIFY_FAILED");}
 private static long be64(byte[] b,int at)throws IOException{long high=be32(b,at);if(high>0x7fffffffL)throw new IOException("VERIFY_FAILED");return (high<<32)|be32(b,at+4);}
 static void verifyBtrfs(BlockDevice volume)throws IOException{byte[] primary=null;for(long at:new long[]{65536,64L*1048576}){if(at+4096>volume.blockCount()*512)continue;byte[] b=new byte[4096];volume.read(at/512,b,b.length);if(!new String(b,64,8,java.nio.charset.StandardCharsets.US_ASCII).equals("_BHRfS_M")||u64(b,48)!=at||u64(b,112)!=volume.blockCount()*512||u32(b,144)!=4096||u32(b,148)!=16384||b[196]!=0||b[197]!=0)throw new IOException("VERIFY_FAILED");int crc=-1;for(int i=32;i<b.length;i++){crc^=b[i]&255;for(int j=0;j<8;j++)crc=(crc>>>1)^((crc&1)!=0?0x82f63b78:0);}if(u32(b,0)!=((~crc)&0xffffffffL))throw new IOException("VERIFY_FAILED");if(primary!=null&&!Arrays.equals(Arrays.copyOfRange(primary,32,48),Arrays.copyOfRange(b,32,48)))throw new IOException("VERIFY_FAILED");primary=b;}}
 static void verifyApfs(BlockDevice volume)throws IOException{byte[] b=new byte[4096];volume.read(0,b,b.length);if(!new String(b,32,4,java.nio.charset.StandardCharsets.US_ASCII).equals("NXSB")||u32(b,36)!=4096||u64(b,40)!=volume.blockCount()/8)throw new IOException("VERIFY_FAILED");long a=0,c=0;for(int i=8;i<b.length;i+=4){a=(a+u32(b,i))%0xffffffffL;c=(c+a)%0xffffffffL;}long c1=0xffffffffL-(a+c)%0xffffffffL,c2=0xffffffffL-(a+c1)%0xffffffffL;if(u32(b,0)!=c1||u32(b,4)!=c2)throw new IOException("VERIFY_FAILED");}
 static void verifyHfs(BlockDevice volume)throws IOException{byte[] first=new byte[512],last=new byte[512];volume.read(2,first,512);volume.read(volume.blockCount()-2,last,512);if(first[0]!='H'||first[1]!='+'||first[2]!=0||first[3]!=4||!Arrays.equals(first,last))throw new IOException("VERIFY_FAILED");long block=be32(first,40),count=be32(first,44),free=be32(first,48);if(block!=4096||count<1||count>volume.blockCount()/8||free>count||be32(first,284)==0)throw new IOException("VERIFY_FAILED");}
 private static long be32(byte[] b,int at){long n=0;for(int i=0;i<4;i++)n=n*256+(b[at+i]&255);return n;}
 private static long u32(byte[] b,int at){long n=0;for(int i=0;i<4;i++)n|=(long)(b[at+i]&255)<<(8*i);return n;}
 private static long u64(byte[] b,int at)throws IOException{long low=u32(b,at),high=u32(b,at+4);if(high>0x7fffffffL)throw new IOException("VERIFY_FAILED");return low|(high<<32);}
 static void verifyUdf(BlockDevice volume)throws IOException{
  for(long where:new long[]{256,volume.blockCount()-1,volume.blockCount()-257}){byte[] b=new byte[512];volume.read(where,b,512);if((b[0]&255)!=2||b[1]!=0)throw new IOException("VERIFY_FAILED");int sum=0;for(int i=0;i<16;i++)if(i!=4)sum+=b[i]&255;if((sum&255)!=(b[4]&255))throw new IOException("VERIFY_FAILED");long location=0;for(int i=0;i<4;i++)location|=(long)(b[12+i]&255)<<(8*i);int length=(b[10]&255)|((b[11]&255)<<8),expected=(b[8]&255)|((b[9]&255)<<8);if(location!=where||length>496||UdfImage.crc(b,16,length)!=expected)throw new IOException("VERIFY_FAILED");}
 }
}
