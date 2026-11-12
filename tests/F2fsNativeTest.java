package org.usbworkbench;
import java.io.*;import android.content.*;import android.content.pm.*;import java.util.*;
public final class F2fsNativeTest{
 static int checks;static void yes(boolean b){if(!b)throw new AssertionError();checks++;}
 public static void main(String[] args)throws Exception{
  File dir=new File("artifacts/native-fixtures/f2fs");dir.mkdirs();Context context=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return dir;}};
  for(String table:new String[]{"mbr","gpt"}){File file=new File(dir,"owned-"+table+".img");try(NativeIntegrationTest.Image disk=new NativeIntegrationTest.Image(file,256L*1048576)){NativeFormat.format(context,disk,"f2fs",table,"ورشة",0,(a,b)->{},()->false);SliceDevice volume=new SliceDevice(disk,2048,disk.blockCount()-2048-(table.equals("gpt")?33:0));F2fsInspector.inspect(volume);yes(true);int before=disk.writes;String log=NativeTools.run(context,"fsck_f2fs",Arrays.asList("--dry-run","--no-kernel-check","/dev/usbworkbench"),volume,false,(a,b)->{},()->false);yes(disk.writes==before);yes(log.contains("Done"));if(table.equals("gpt"))yes(AdvancedDiskTools.inspectGpt(disk).contains("CRC OK"));else{byte[] boot=new byte[512];disk.read(0,boot,512);yes((boot[450]&255)==131);}System.out.println("PASS actual F2FS1.17 format/dry-run/no-write/GPT-MBR "+table);}finally{file.delete();}}
  File file=new File(dir,"small.img");try(NativeIntegrationTest.Image disk=new NativeIntegrationTest.Image(file,64L*1048576)){try{NativeFormat.format(context,disk,"f2fs","gpt","SAFE",0,(a,b)->{},()->false);throw new AssertionError();}catch(IOException e){yes(e.getMessage().equals("F2FS_SIZE")&&disk.writes==0);}}finally{file.delete();}
  System.out.println("PASS: "+checks+" actual F2FS format/check/no-write/size/table assertions; owned sparse files only");
 }
}
