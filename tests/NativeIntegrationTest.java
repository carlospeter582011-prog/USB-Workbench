package org.usbworkbench;
import java.io.*;
import java.util.*;
import android.content.*;
import android.content.pm.*;
/** Runs actual ARM64 executables through the actual Java runner against owned sparse files. */
public class NativeIntegrationTest {
 static int checks;static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class Image implements BlockDevice {
  final RandomAccessFile f;final long count;int writes;long failAt=-1;
  Image(File path,long bytes)throws IOException{f=new RandomAccessFile(path,"rw");f.setLength(bytes);count=bytes/512;}
  public int blockSize(){return 512;}public long blockCount(){return count;}public void read(long lba,byte[] b,int n)throws IOException{if(lba<0||n%512!=0||lba+n/512>count)throw new IOException("DISK_BOUNDS");f.seek(lba*512);f.readFully(b,0,n);}
  public void write(long lba,byte[] b,int n)throws IOException{if(failAt>=0&&lba==failAt)throw new IOException("INJECTED_IO");if(lba<0||n%512!=0||lba+n/512>count)throw new IOException("DISK_BOUNDS");writes++;f.seek(lba*512);f.write(b,0,n);}public void sync()throws IOException{f.getFD().sync();}public void close(){try{f.close();}catch(IOException e){throw new java.io.UncheckedIOException(e);}}
 }
 public static void main(String[] args)throws Exception{
  File dir=new File("artifacts/native-fixtures/java");dir.mkdirs();Context context=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return dir;}};
  for(String fs:new String[]{"ntfs","ext2","ext3","ext4"})for(String table:new String[]{"mbr","gpt"}){
   File file=new File(dir,fs+"-"+table+".img");try(Image disk=new Image(file,64L*1024*1024)){NativeFormat.format(context,disk,fs,table,"ورشة",0,(a,b)->{},()->false);byte[] boot=new byte[2048];disk.read(2048,boot,boot.length);check(fs.equals("ntfs")?new String(boot,3,8,"US-ASCII").equals("NTFS    "):(boot[1080]&255)==0x53&&(boot[1081]&255)==0xef,fs+" signature");if(table.equals("gpt"))check(AdvancedDiskTools.inspectGpt(disk).contains("CRC OK"),"GPT CRC");else{byte[] b=new byte[512];disk.read(0,b,512);check((b[450]&255)==(fs.equals("ntfs")?7:131),"MBR type");}System.out.println("PASS native format "+fs+" / "+table+" / Unicode label");}finally{file.delete();}
  }
  File file=new File(dir,"invalid.img");try(Image d=new Image(file,64L*1024*1024)){try{NativeFormat.format(context,d,"ext4","gpt","too-long-label-1234567",0,(a,b)->{},()->false);throw new AssertionError();}catch(IOException e){check(e.getMessage().equals("EXT_LABEL"),"label error");}check(d.writes==0,"invalid options write nothing");}finally{file.delete();}
  try(Image d=new Image(file,64L*1024*1024)){d.failAt=2048;try{NativeFormat.format(context,d,"ntfs","gpt","FAILTEST",0,(a,b)->{},()->false);throw new AssertionError();}catch(IOException e){check(e.getMessage().equals("INJECTED_IO"),"fault propagated");}byte[] b=new byte[512];d.read(0,b,512);check(b[510]==0&&b[511]==0,"failed format never commits table");}finally{file.delete();}
  System.out.println("PASS: "+checks+" Java/native integration assertions; no physical device used");
 }
}
