package org.usbworkbench;
import java.io.*;import java.util.*;import android.content.*;import android.content.pm.*;
public final class PersistenceTest {
 public static void main(String[] args)throws Exception{
  File root=new File("artifacts/native-fixtures/persistence-test");root.mkdirs();File image=new File(root,"disposable.dat");Context c=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return root;}};
  int cases=0;
  for(int size:new int[]{256,257,768,16384}){if(PersistenceImage.size(size,"custom-live")!=size*1048576L)throw new AssertionError("custom size");cases++;}
  for(int size:new int[]{-1,0,255,16385,Integer.MAX_VALUE}){try{PersistenceImage.size(size,"persistence");throw new AssertionError("bad size accepted");}catch(IOException expected){cases++;}}
  for(String label:new String[]{"","bad label","../x","abcdefghijklmnopq","استمرار"}){try{PersistenceImage.size(768,label);throw new AssertionError("bad label accepted");}catch(IOException expected){cases++;}}
  for(int customSize:new int[]{256,384})try(NativeIntegrationTest.Image disk=new NativeIntegrationTest.Image(image,PersistenceImage.size(customSize,"persistence"))){
   PersistenceImage.create(c,disk,"persistence",true,(a,b)->{},()->false);byte[] superblock=new byte[2048];disk.read(0,superblock,superblock.length);if((superblock[1080]&255)!=83||(superblock[1081]&255)!=239)throw new AssertionError("ext4 signature");
   Process p=new ProcessBuilder("/data/data/com.termux/files/usr/glibc/bin/debugfs","-R","cat /persistence.conf",image.getAbsolutePath()).redirectErrorStream(true).start();ByteArrayOutputStream log=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;while((n=p.getInputStream().read(b))!=-1)log.write(b,0,n);if(p.waitFor()!=0||!log.toString("UTF-8").contains("/ union"))throw new AssertionError("independent debugfs "+log.toString("UTF-8"));
   if((java.nio.ByteBuffer.wrap(superblock).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt(1028)&0xffffffffL)!=(customSize*1048576L/4096))throw new AssertionError("custom ext4 size");cases++;
   if(!new String(superblock,1144,11,java.nio.charset.StandardCharsets.US_ASCII).equals("persistence"))throw new AssertionError("ext4 label");cases++;
   File[] leftovers=root.listFiles((dir,name)->name.startsWith("persistence-seed-"));if(leftovers==null||leftovers.length!=0)throw new AssertionError("seed cleanup");System.out.println("PASS: ext4 persistence creation, native fsck, independent debugfs config content and seed cleanup");
  }finally{if(image.exists()&&!image.delete())throw new IOException("fixture cleanup");}
  System.out.println("PASS: "+cases+" custom persistence size/label/bounds and actual 256/384MiB ext4 checks");
 }
}
