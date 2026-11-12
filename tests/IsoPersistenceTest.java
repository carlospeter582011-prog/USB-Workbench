package org.usbworkbench;
import java.io.*;import java.util.*;import android.content.*;import android.content.pm.*;
public final class IsoPersistenceTest {
 static int checks;static void ok(boolean b){if(!b)throw new AssertionError();checks++;}
 interface Check{void run()throws Exception;}static void fails(String code,Check c)throws Exception{try{c.run();throw new AssertionError("accepted "+code);}catch(IOException e){if(!e.getMessage().startsWith(code))throw e;checks++;}}
 static void le(byte[] b,int p,long v){for(int i=0;i<4;i++)b[p+i]=(byte)(v>>(8*i));}
 static long u32(byte[] b,int p){long v=0;for(int i=0;i<4;i++)v|=(b[p+i]&255L)<<(8*i);return v;}
 static byte[] header(){byte[] b=new byte[65536];b[0]=0x33;b[510]=85;b[511]=(byte)170;b[446]=(byte)128;b[450]=0x17;le(b,454,0);le(b,458,4096);b[466]=(byte)0xef;le(b,470,100);le(b,474,10);int p=32768;b[p]=1;b[p+1]='C';b[p+2]='D';b[p+3]='0';b[p+4]='0';b[p+5]='1';b[p+6]=1;return b;}
 static class Memory implements BlockDevice{
  long blocks=2097152;int sector=512,writes;boolean failCommit,corrupt;Map<Long,byte[]> data=new HashMap<>();
  Memory(byte[] h)throws Exception{write(0,h,h.length);writes=0;}
  public int blockSize(){return sector;}public long blockCount(){return blocks;}
  public void read(long at,byte[] out,int n)throws IOException{if(at<0||n%512!=0||at+n/512>blocks)throw new IOException("bounds");for(int p=0;p<n;p+=512){byte[] b=data.get(at+p/512);if(b==null)Arrays.fill(out,p,p+512,(byte)0);else System.arraycopy(b,0,out,p,512);}}
  public void write(long at,byte[] in,int n)throws IOException{if(at<0||n%512!=0||at+n/512>blocks)throw new IOException("bounds");writes++;for(int p=0;p<n;p+=512)data.put(at+p/512,Arrays.copyOfRange(in,p,p+512));if(at==0&&n==512){if(corrupt)data.get(0L)[0]^=1;if(failCommit)throw new IOException("owned failure");}}
  public void sync(){}public void close(){}byte[] prefix()throws Exception{byte[] b=new byte[65536];read(0,b,b.length);return b;}
 }
 public static void main(String[] args)throws Exception{
  byte[] original=header();Memory d=new Memory(original);IsoPersistence.Plan p=IsoPersistence.plan(d,original,2097152,256,"persistence");ok(p.start==4096&&p.count==524288&&p.partition==3);ok(d.writes==0);ok(Arrays.equals(original,header()));
  for(int k=0;k<13;k++){final Memory bad=new Memory(header());final byte[] h=header();final int which=k;final long size=k==10?2097153L:2097152L;
   if(k==0)h[510]=0;if(k==1)h[450]=(byte)0xee;if(k==2)System.arraycopy("EFI PART".getBytes("US-ASCII"),0,h,512,8);if(k==3)h[450]=5;if(k==4)h[446]=2;if(k==5)h[32769]=0;if(k==6)h[0]=0;if(k==7)h[446]=0;if(k==8)le(h,458,999999);if(k==9)bad.blocks=500000;if(k==11)bad.sector=4096;if(k==12){h[478]=1;}
   fails(k==1||k==2?"ISO_PERSISTENCE_GPT":k==9?"ISO_PERSISTENCE_SPACE":k==11?"ISO_PERSISTENCE_GEOMETRY":"ISO_PERSISTENCE_LAYOUT",()->IsoPersistence.plan(bad,h,size,256,"persistence"));ok(bad.writes==0);
  }
  byte[] full=header();for(int at:new int[]{478,494}){full[at+4]=0x17;le(full,at+8,64);le(full,at+12,4);}fails("ISO_PERSISTENCE_LAYOUT",()->IsoPersistence.plan(d,full,2097152,256,"persistence"));
  fails("PERSISTENCE_OPTIONS",()->IsoPersistence.plan(d,header(),2097152,255,"persistence"));fails("PERSISTENCE_OPTIONS",()->IsoPersistence.plan(d,header(),2097152,256,"bad label"));
  final int[] calls={0};IsoPersistence.apply(d,p,(part,label)->{calls[0]++;ok(part.blockCount()==524288&&label.equals("persistence"));byte[] b=new byte[512];b[0]=42;part.write(0,b,512);part.sync();},(a,b)->{});ok(calls[0]==1&&d.writes==2);
  byte[] after=d.prefix();for(int i=0;i<after.length;i++)if(i<478||i>=494)if(after[i]!=original[i])throw new AssertionError("source changed "+i);checks++;ok((after[482]&255)==0x83&&u32(after,486)==4096&&u32(after,490)==524288);ok(d.data.get(4096L)[0]==42);
  final Memory stale=new Memory(header());IsoPersistence.Plan sp=IsoPersistence.plan(stale,header(),2097152,256,"persistence");stale.data.get(64L)[10]=1;fails("SOURCE_CHANGED",()->IsoPersistence.apply(stale,sp,(v,l)->{throw new AssertionError("formatter called");},(a,b)->{}));ok(stale.writes==0);
  final Memory failure=new Memory(header());IsoPersistence.Plan fp=IsoPersistence.plan(failure,header(),2097152,256,"persistence");fails("ISO_PERSISTENCE_INCOMPLETE",()->IsoPersistence.apply(failure,fp,(v,l)->{v.write(0,new byte[512],512);throw new IOException("owned format failure");},(a,b)->{}));ok(failure.writes==1&&Arrays.equals(failure.prefix(),header()));
  final Memory cancelled=new Memory(header());IsoPersistence.Plan cp=IsoPersistence.plan(cancelled,header(),2097152,256,"persistence");fails("CANCELLED",()->IsoPersistence.apply(cancelled,cp,(v,l)->{throw new AssertionError();},(a,b)->{throw new IOException("CANCELLED");}));ok(cancelled.writes==0);
  final int[] progress={0};fails("ISO_PERSISTENCE_INCOMPLETE",()->IsoPersistence.apply(cancelled,cp,(v,l)->v.write(0,new byte[512],512),(a,b)->{if(++progress[0]==2)throw new IOException("CANCELLED");}));ok(cancelled.writes==1&&Arrays.equals(cancelled.prefix(),header()));
  for(boolean mismatch:new boolean[]{false,true}){final Memory broken=new Memory(header());IsoPersistence.Plan bp=IsoPersistence.plan(broken,header(),2097152,256,"persistence");broken.failCommit=!mismatch;broken.corrupt=mismatch;fails("ISO_PERSISTENCE_INCOMPLETE",()->IsoPersistence.apply(broken,bp,(v,l)->{},(a,b)->{}));ok(broken.writes==1);}
  final Memory bound=new Memory(header());IsoPersistence.Plan bp=IsoPersistence.plan(bound,header(),2097152,256,"persistence");fails("ISO_PERSISTENCE_INCOMPLETE",()->IsoPersistence.apply(bound,bp,(v,l)->v.write(v.blockCount(),new byte[512],512),(a,b)->{}));ok(bound.writes==0);
  File root=new File("artifacts/native-fixtures/direct-persistence");root.mkdirs();File file=new File(root,"owned-direct.img");Context c=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return root;}};
  try(NativeIntegrationTest.Image disk=new NativeIntegrationTest.Image(file,1073741824L)){
   byte[] iso=new byte[2097152];System.arraycopy(header(),0,iso,0,65536);IsoPersistence.Plan plan=IsoPersistence.plan(disk,header(),iso.length,384,"persistence");DiskTools.writeImage(disk,new ByteArrayInputStream(iso),iso.length,(a,b)->{});DiskTools.verifyImage(disk,new ByteArrayInputStream(iso),iso.length,(a,b)->{});
   IsoPersistence.apply(disk,plan,(volume,label)->PersistenceImage.create(c,volume,label,true,(a,b)->{},()->false),(a,b)->{});
   byte[] mbr=new byte[512];disk.read(0,mbr,512);System.arraycopy(mbr,0,iso,0,512);DiskTools.verifyImage(disk,new ByteArrayInputStream(iso),iso.length,(a,b)->{});checks++;
   ok(u32(mbr,486)==plan.start&&u32(mbr,490)==384L*2048);
   byte[] sb=new byte[2048];disk.read(plan.start,sb,2048);ok((sb[1080]&255)==83&&(sb[1081]&255)==239);ok(u32(sb,1028)==384L*1048576/4096);ok(new String(sb,1144,11,"US-ASCII").equals("persistence"));
  }finally{if(file.exists()&&!file.delete())throw new IOException("fixture cleanup");}
  ok(root.listFiles((dir,name)->name.startsWith("persistence-seed-")).length==0);
  System.out.println("PASS: "+checks+" direct ISO persistence layout/zero-write rejection/byte-preservation/bounds/cancel/commit/no-retry and actual384MiB ext4 checks; no hardware or boot test");
 }
}
