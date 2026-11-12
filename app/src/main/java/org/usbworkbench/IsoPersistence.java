package org.usbworkbench;
import java.io.*;import java.util.*;
/** Append-only persistence for a bounded subset of MBR Linux hybrid ISOs. Never edits GPT. */
public final class IsoPersistence {
 public interface Formatter {void create(BlockDevice partition,String label)throws IOException;}
 public static final class Plan {
  public final long start,count,blocks;public final int partition;public final String label;
  private final byte[] prefix,mbr;
  private Plan(long start,long count,long blocks,int slot,String label,byte[] prefix,byte[] mbr){this.start=start;this.count=count;this.blocks=blocks;this.partition=slot+1;this.label=label;this.prefix=prefix;this.mbr=mbr;}
 }
 private static long u32(byte[] b,int p){long v=0;for(int i=0;i<4;i++)v|=(b[p+i]&255L)<<(8*i);return v;}
 private static void le32(byte[] b,int p,long v){for(int i=0;i<4;i++)b[p+i]=(byte)(v>>(8*i));}
 public static Plan plan(BlockDevice disk,byte[] header,long size,int mib,String label)throws IOException{
  long bytes=PersistenceImage.size(mib,label);
  if(disk.blockSize()!=512||disk.blockCount()<1||disk.blockCount()>0xffffffffL)throw new IOException("ISO_PERSISTENCE_GEOMETRY");
  if(header==null||header.length<65536||size<65536||size%2048!=0||size>disk.blockCount()*512L)throw new IOException("ISO_PERSISTENCE_LAYOUT");
  byte[] prefix=Arrays.copyOf(header,65536),mbr=Arrays.copyOf(prefix,512);
  if((mbr[510]&255)!=85||(mbr[511]&255)!=170)throw new IOException("ISO_PERSISTENCE_LAYOUT");
  if(new String(prefix,512,8,java.nio.charset.StandardCharsets.US_ASCII).equals("EFI PART"))throw new IOException("ISO_PERSISTENCE_GPT");
  boolean pvd=false,boot=false,active=false;int free=-1,used=0;
  for(int i=0;i<440;i++)if(mbr[i]!=0)boot=true;
  for(int i=16;i<32;i++){int at=i*2048;if(prefix[at+1]=='C'&&prefix[at+2]=='D'&&prefix[at+3]=='0'&&prefix[at+4]=='0'&&prefix[at+5]=='1'&&prefix[at+6]==1){if(prefix[at]==1)pvd=true;if((prefix[at]&255)==255)break;}}
  long end=size/512;
  for(int i=0;i<4;i++){
   int at=446+i*16,type=mbr[at+4]&255,flag=mbr[at]&255;boolean zero=true;for(int j=0;j<16;j++)if(mbr[at+j]!=0)zero=false;
   if(zero){if(free<0)free=i;continue;}
   if(type==0xee)throw new IOException("ISO_PERSISTENCE_GPT");
   if(type!=0x17&&type!=0x83&&type!=0xef&&type!=0x0b&&type!=0x0c&&type!=0x06&&type!=0x0e)throw new IOException("ISO_PERSISTENCE_LAYOUT");
   long start=u32(mbr,at+8),count=u32(mbr,at+12);
   if((flag!=0&&flag!=128)||count==0||start>disk.blockCount()||count>disk.blockCount()-start||start+count>size/512+2048)throw new IOException("ISO_PERSISTENCE_LAYOUT");
   end=Math.max(end,start+count);if(flag==128)active=true;used++;
  }
  // Existing EFI/data overlap is common in hybrid images. New space is beyond ALL extents.
  if(!boot||!pvd||!active||used==0||free<0)throw new IOException("ISO_PERSISTENCE_LAYOUT");
  long start=(end+2047)/2048*2048,count=bytes/512;
  if(start>disk.blockCount()||count>disk.blockCount()-start)throw new IOException("ISO_PERSISTENCE_SPACE");
  int at=446+free*16;byte[] changed=mbr.clone();changed[at+1]=(byte)254;changed[at+2]=(byte)255;changed[at+3]=(byte)255;changed[at+4]=(byte)0x83;changed[at+5]=(byte)254;changed[at+6]=(byte)255;changed[at+7]=(byte)255;le32(changed,at+8,start);le32(changed,at+12,count);
  return new Plan(start,count,disk.blockCount(),free,label,prefix,changed);
 }
 private static void same(BlockDevice disk,Plan plan)throws IOException{
  if(disk.blockSize()!=512||disk.blockCount()!=plan.blocks)throw new IOException("DEVICE_CHANGED");
  byte[] actual=new byte[65536];disk.read(0,actual,actual.length);if(!Arrays.equals(actual,plan.prefix))throw new IOException("SOURCE_CHANGED");
 }
 public static void apply(BlockDevice disk,Plan plan,Formatter formatter,DiskTools.Progress p)throws IOException{
  p.update(0,-1);same(disk,plan);
  try{
   formatter.create(new SliceDevice(disk,plan.start,plan.count),plan.label);
   p.update(0,-1);same(disk,plan); // do not publish a partition until formatting/checks succeed
   disk.write(0,plan.mbr,512);disk.sync();byte[] actual=new byte[512];disk.read(0,actual,512);
   if(!Arrays.equals(actual,plan.mbr))throw new IOException("VERIFY_FAILED");p.update(1,1);
  }catch(IOException e){throw new IOException("ISO_PERSISTENCE_INCOMPLETE "+e.getMessage(),e);}
 }
}
