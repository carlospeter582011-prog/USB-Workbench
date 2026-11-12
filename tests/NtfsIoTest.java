package org.usbworkbench;
import java.io.*;import java.util.*;import android.content.*;import android.content.pm.*;
/** Real bundled NTFS formatter through IPC, owned files only; compares physical IO, not claimed hardware speed. */
public final class NtfsIoTest {
 static int checks;static void yes(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  File dir=new File("artifacts/native-fixtures/ntfs-io");dir.mkdirs();Context context=new Context(){final ApplicationInfo info=new ApplicationInfo();{info.nativeLibraryDir=new File("app/src/main/jniLibs/arm64-v8a").getAbsolutePath();}public ApplicationInfo getApplicationInfo(){return info;}public File getFilesDir(){return dir;}};
  long[] calls=new long[2],written=new long[2];
  for(int mode=0;mode<2;mode++){File file=new File(dir,"owned-"+mode+".img");try(NativeIntegrationTest.Image image=new NativeIntegrationTest.Image(file,64L*1048576)){
   IoMetrics metrics=new IoMetrics();BlockDevice counted=metrics.wrap(image);long start=System.nanoTime();List<Long> done=new ArrayList<>();
   if(mode==0)NativeTools.run(context,"mkntfs",Arrays.asList("-Q","-F","-s","512","-p","2048","-L","ورشة","/dev/usbworkbench"),counted,true,(a,b)->done.add(a),()->false);
   else NativeTools.format(context,counted,"ntfs","ورشة",0,(a,b)->done.add(a),()->false);
   calls[mode]=metrics.reads+metrics.writes+metrics.syncs;written[mode]=metrics.writeBytes;
   yes(metrics.writeBytes>0&&metrics.readBytes>=metrics.writeBytes,"all physical write bytes read back "+mode);yes(metrics.writeBytes<64L*1048576,"quick format, not full-volume zero-fill "+mode);yes(!done.isEmpty()&&done.get(done.size()-1)>0,"native IO progress advances "+mode);
   NtfsInspector.inspect(image);yes(true,"primary/backup boot and MFT fixups "+mode);NativeTools.run(context,"ntfsinfo",Arrays.asList("-f","-m","/dev/usbworkbench"),image,false,(a,b)->{},()->false);yes(true,"independent native checker "+mode);
   System.out.println((mode==0?"UNBUFFERED":"BATCHED")+" physicalIO="+calls[mode]+" writeBytes="+metrics.writeBytes+" readBytes="+metrics.readBytes+" fixtureMs="+(System.nanoTime()-start)/1000000);
  }finally{if(file.exists()&&!file.delete())throw new IOException("Owned NTFS fixture cleanup");}}
  yes(calls[1]<calls[0]/2,"material reduction of real formatter physical requests");yes(written[1]<=written[0],"combining does not write extra disk bytes");System.out.println("PASS: "+checks+" actual NTFS quick-format IO/metadata/readback/progress checks; no USB hardware used");
 }
}
