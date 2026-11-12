package org.usbworkbench;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class ChdTest {
 static int checks;static void yes(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception{
 File dir=new File("artifacts/chd-fixtures"),libs=new File("app/src/main/jniLibs/arm64-v8a");
 for(String name:new String[]{"512","2048","compressed-zlib","compressed-lzma","compressed-zstd","compressed-huff"}){int unit=name.equals("512")?512:2048;File input=new File(dir,name+".chd"),out=new File(dir,name+".output.img");out.delete();try(OpticalImage.Source source=ArchiveTest.source(input)){ChdTools.Plan p=ChdTools.inspect(libs,source,1048576,(a,b)->{},()->false);yes(p.unit==unit&&p.bytes==5*unit,"header/partial final hunk");String hash=ChdTools.convert(libs,source,p,new WimFiles.Store(){public void capacity(long n){}public WimFiles.RandomFile create(String n)throws IOException{return ArchiveTest.output(out);}},(a,b)->{},()->false);try(InputStream in=new FileInputStream(out)){ChdTools.verify(in,p,hash,(a,b)->{});}yes(true,"providedrawSHA1+outputSHA256");yes(Arrays.equals(Files.readAllBytes(out.toPath()),Files.readAllBytes(new File(dir,unit+".expected.img").toPath())),"independentPython allbytes");byte[] bad=Files.readAllBytes(out.toPath());bad[0]^=1;try{ChdTools.verify(new ByteArrayInputStream(bad),p,hash,(a,b)->{});throw new AssertionError();}catch(IOException e){yes(e.getMessage().equals("VERIFY_FAILED"),"bad output");}}}
 try(OpticalImage.Source source=ArchiveTest.source(new File(dir,"2448.chd"))){try{ChdTools.inspect(libs,source,1048576,(a,b)->{},()->false);throw new AssertionError();}catch(IOException e){yes(e.getMessage().startsWith("CHD_ENGINE_FAILED"),"CD track geometry refused");}}
 try(OpticalImage.Source source=ArchiveTest.source(new File(dir,"512.chd"))){try{ChdTools.inspect(libs,source,16,(a,b)->{},()->false);throw new AssertionError();}catch(IOException e){yes(e.getMessage().startsWith("CHD_ENGINE_FAILED"),"limit preflight");}try{ChdTools.inspect(libs,source,1048576,(a,b)->{},()->true);throw new AssertionError();}catch(IOException e){yes(e.getMessage().equals("CANCELLED"),"cancel");}}
 System.out.println("PASS: "+checks+" actual CHDv5/512-HD/2048-DVD logical decode, partial hunk, rawSHA1/SHA256, readback/CD-limit rejection assertions");
 }
}
