package org.usbworkbench;
import java.io.*;import java.util.*;import java.nio.charset.StandardCharsets;
/** Original bounded metadata editor. Never resizes, creates, deletes, mounts or writes volume data. */
public final class PartitionMetadata {
 public static final class Plan {
  private final long blocks;private final int sector;private final String mode,name;private final int partition;
  private final TreeMap<Long,byte[]> before=new TreeMap<>(),after=new TreeMap<>();
  private Plan(BlockDevice d,String mode,int part,String name){blocks=d.blockCount();sector=d.blockSize();this.mode=mode;partition=part;this.name=name;}
 }
 public static void name(String s)throws IOException{if(s==null||s.length()<1||s.length()>35||!s.equals(s.trim()))throw new IOException("PARTITION_NAME");for(int i=0;i<s.length();i++){char c=s.charAt(i);if(Character.isISOControl(c)||Character.isSurrogate(c)||(c>=0x202a&&c<=0x202e)||(c>=0x2066&&c<=0x2069))throw new IOException("PARTITION_NAME");}}
 private static byte[] read(BlockDevice d,long at,int n)throws IOException{if(at<0||n<1||n%d.blockSize()!=0||at>d.blockCount()||n/d.blockSize()>d.blockCount()-at)throw new IOException("PARTITION_LAYOUT");byte[] b=new byte[n];d.read(at,b,n);return b;}
 private static void add(Plan p,BlockDevice d,long at,int n)throws IOException{byte[] b=read(d,at,n);p.before.put(at,b);p.after.put(at,b.clone());}
 private static boolean empty(byte[] b,int at,int n){for(int i=0;i<n;i++)if(b[at+i]!=0)return false;return true;}
 private static void noOverlap(List<long[]> spans)throws IOException{Collections.sort(spans,(a,b)->Long.compare(a[0],b[0]));long end=0;for(long[] r:spans){if(r[0]<1||r[0]<=end||r[1]<r[0])throw new IOException("PARTITION_LAYOUT");end=r[1];}}
 public static Plan plan(BlockDevice d,String mode,int part,String label)throws IOException{
  if(d.blockSize()!=512||d.blockCount()<4096||d.blockCount()>0xffffffffL||part<1||part>128||(!"gptName".equals(mode)&&!"mbrActive".equals(mode)))throw new IOException("PARTITION_LAYOUT");if("gptName".equals(mode))name(label);
  Plan p=new Plan(d,mode,part,"gptName".equals(mode)?label:"");add(p,d,0,512);byte[] m=p.before.get(0L);if((m[510]&255)!=85||(m[511]&255)!=170)throw new IOException("PARTITION_LAYOUT");
  if("mbrActive".equals(mode)){
   if(part>4)throw new IOException("PARTITION_LAYOUT");List<long[]> spans=new ArrayList<>();boolean selected=false;
   for(int i=0;i<4;i++){int at=446+i*16,type=m[at+4]&255,flag=m[at]&255;long first=AdvancedDiskTools.u32(m,at+8),count=AdvancedDiskTools.u32(m,at+12);if(flag!=0&&flag!=128)throw new IOException("PARTITION_LAYOUT");if(type==0){if(first!=0||count!=0||flag!=0)throw new IOException("PARTITION_LAYOUT");continue;}if(!Arrays.asList(1,4,6,7,11,12,14,0x82,0x83,0xef).contains(type)||first<1||count<1||first>=d.blockCount()||count>d.blockCount()-first)throw new IOException("PARTITION_LAYOUT");spans.add(new long[]{first,first+count-1});if(i+1==part)selected=true;}
   noOverlap(spans);if(!selected)throw new IOException("PARTITION_LAYOUT");byte[] changed=p.after.get(0L);for(int i=0;i<4;i++)changed[446+i*16]=(byte)(i+1==part?128:0);return p;
  }
  // Deliberately accept only a conventional protective MBR and matching standard GPT copies.
  if((m[446]&255)!=0||(m[450]&255)!=238||AdvancedDiskTools.u32(m,454)!=1||AdvancedDiskTools.u32(m,458)!=d.blockCount()-1)throw new IOException("PARTITION_LAYOUT");for(int i=1;i<4;i++)if(!empty(m,446+i*16,16))throw new IOException("PARTITION_LAYOUT");
  AdvancedDiskTools.inspectGpt(d);add(p,d,1,512);add(p,d,2,16384);add(p,d,d.blockCount()-33,16384);add(p,d,d.blockCount()-1,512);
  byte[] h=p.before.get(1L),bh=p.before.get(d.blockCount()-1),table=p.before.get(2L);
  if(AdvancedDiskTools.u32(h,12)!=92||AdvancedDiskTools.u32(bh,12)!=92||AdvancedDiskTools.u32(h,8)!=0x10000||AdvancedDiskTools.u32(bh,8)!=0x10000||AdvancedDiskTools.u32(h,20)!=0||AdvancedDiskTools.u32(bh,20)!=0||AdvancedDiskTools.u64(h,72)!=2||AdvancedDiskTools.u64(bh,72)!=d.blockCount()-33||AdvancedDiskTools.u32(h,80)!=128||AdvancedDiskTools.u32(h,84)!=128||AdvancedDiskTools.u64(h,40)<34||AdvancedDiskTools.u64(h,48)>=d.blockCount()-33||!Arrays.equals(table,p.before.get(d.blockCount()-33)))throw new IOException("PARTITION_LAYOUT");
  for(byte[] header:new byte[][]{h,bh}){byte[] checked=header.clone();DiskTools.le32(checked,16,0);if(AdvancedDiskTools.u32(header,16)!=AdvancedDiskTools.crc(checked,92)||AdvancedDiskTools.u32(header,88)!=AdvancedDiskTools.crc(table,table.length)||AdvancedDiskTools.u32(header,80)!=128||AdvancedDiskTools.u32(header,84)!=128)throw new IOException("PARTITION_LAYOUT");}
  if(AdvancedDiskTools.u64(h,24)!=1||AdvancedDiskTools.u64(h,32)!=d.blockCount()-1||AdvancedDiskTools.u64(bh,24)!=d.blockCount()-1||AdvancedDiskTools.u64(bh,32)!=1||AdvancedDiskTools.u64(h,40)!=AdvancedDiskTools.u64(bh,40)||AdvancedDiskTools.u64(h,48)!=AdvancedDiskTools.u64(bh,48)||!Arrays.equals(Arrays.copyOfRange(h,56,72),Arrays.copyOfRange(bh,56,72)))throw new IOException("PARTITION_LAYOUT");
  List<long[]> spans=new ArrayList<>();Set<String> ids=new HashSet<>();for(int i=0;i<128;i++){int at=i*128;if(empty(table,at,16))continue;if(empty(table,at+16,16)||!ids.add(Arrays.toString(Arrays.copyOfRange(table,at+16,at+32))))throw new IOException("PARTITION_LAYOUT");long first=AdvancedDiskTools.u64(table,at+32),last=AdvancedDiskTools.u64(table,at+40);if(first<AdvancedDiskTools.u64(h,40)||last>AdvancedDiskTools.u64(h,48))throw new IOException("PARTITION_LAYOUT");spans.add(new long[]{first,last});}noOverlap(spans);
  int at=(part-1)*128;if(empty(table,at,16))throw new IOException("PARTITION_LAYOUT");byte[] changed=p.after.get(2L);Arrays.fill(changed,at+56,at+128,(byte)0);byte[] bytes=label.getBytes(StandardCharsets.UTF_16LE);System.arraycopy(bytes,0,changed,at+56,bytes.length);p.after.put(d.blockCount()-33,changed.clone());
  for(long lba:new long[]{1,d.blockCount()-1}){byte[] header=p.after.get(lba);DiskTools.le32(header,88,AdvancedDiskTools.crc(changed,changed.length));DiskTools.le32(header,16,0);DiskTools.le32(header,16,AdvancedDiskTools.crc(header,92));}return p;
 }
 public static String apply(BlockDevice d,Plan plan,DiskTools.Progress progress)throws IOException{
  if(plan.blocks!=d.blockCount()||plan.sector!=d.blockSize())throw new IOException("DEVICE_CHANGED");progress.update(0,1);
  for(Map.Entry<Long,byte[]> e:plan.before.entrySet())if(!Arrays.equals(e.getValue(),read(d,e.getKey(),e.getValue().length)))throw new IOException("PARTITION_CHANGED");
  boolean changed=false;for(long at:plan.before.keySet())if(!Arrays.equals(plan.before.get(at),plan.after.get(at)))changed=true;if(!changed){progress.update(1,1);return "PARTITION_EDIT_UNCHANGED";}
  List<Long> order="gptName".equals(plan.mode)?Arrays.asList(d.blockCount()-33,d.blockCount()-1,2L,1L):Collections.singletonList(0L);boolean attempted=false;
  try{for(int i=0;i<order.size();i++){progress.update(i,order.size());long at=order.get(i);byte[] bytes=plan.after.get(at);attempted=true;d.write(at,bytes,bytes.length);d.sync();if(!Arrays.equals(bytes,read(d,at,bytes.length)))throw new IOException("VERIFY_FAILED");}
   for(Map.Entry<Long,byte[]> e:plan.after.entrySet())if(!Arrays.equals(e.getValue(),read(d,e.getKey(),e.getValue().length)))throw new IOException("VERIFY_FAILED");if("gptName".equals(plan.mode))AdvancedDiskTools.inspectGpt(d);progress.update(order.size(),order.size());return "PARTITION_EDIT_OK\n"+plan.mode+"\n"+plan.partition+"\n"+plan.name;
  }catch(IOException e){if(attempted)throw new IOException("PARTITION_EDIT_INCOMPLETE\n"+e.getMessage(),e);throw e;}
 }
}
