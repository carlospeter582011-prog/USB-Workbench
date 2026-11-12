package org.usbworkbench;
import java.io.*;import android.content.*;import android.content.pm.*;
/** Large logical volumes are sparse owned fixtures, deleted after each case. */
public final class NtfsRegressionTest {
 public static void main(String[] args)throws Exception{
  File dir=new File("artifacts/native-fixtures/ntfs-regression");dir.mkdirs();Context context=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return dir;}};
  int checks=0;for(long size:new long[]{2L*1024*1024*1024,32L*1024*1024*1024})for(int cluster:new int[]{0,1,128}){
   File file=new File(dir,"disposable.img");long start=System.nanoTime();
   try(NativeIntegrationTest.Image disk=new NativeIntegrationTest.Image(file,size)){
    NativeFormat.format(context,disk,"ntfs","gpt","NTFS TEST",cluster,(a,b)->{},()->false);
    byte[] boot=new byte[512];disk.read(2048,boot,512);if(!new String(boot,3,8,"US-ASCII").equals("NTFS    ")||!AdvancedDiskTools.inspectGpt(disk).contains("CRC OK"))throw new AssertionError("NTFS/GPT");checks++;
    System.out.println("PASS NTFS "+size/1073741824+"GiB cluster="+cluster+" in "+(System.nanoTime()-start)/1000000+"ms");
   }finally{if(file.exists()&&!file.delete())throw new IOException("fixture cleanup");}
  }
  System.out.println("PASS: "+checks+" actual large NTFS format/allocation/GPT checks; no USB hardware used");
 }
}
