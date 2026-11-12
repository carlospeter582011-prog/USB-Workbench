package org.usbworkbench;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class DmgTest {
 static int checks;static void yes(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
 static OpticalImage.Source source(byte[] data){return new OpticalImage.Source(){public long size(){return data.length;}public void read(long at,byte[] b,int off,int n)throws IOException{if(at<0||n>data.length-at)throw new IOException("BOUNDS");System.arraycopy(data,(int)at,b,off,n);}public void close(){}};}
 static void fail(String code,byte[] data,long max)throws Exception{try{DmgConverter.budget(source(data),max,(a,b)->{});throw new AssertionError(code);}catch(IOException e){yes(e.getMessage().startsWith(code),e.toString());}}
 public static void main(String[] args)throws Exception{
 byte[] input=Files.readAllBytes(Paths.get("artifacts/dmg-fixtures/owned.dmg")),expected=Files.readAllBytes(Paths.get("artifacts/dmg-fixtures/expected.img"));OpticalImage.Source s=source(input);yes(ImageContainers.supported("dmgRaw"),"dispatch");yes(DmgConverter.budget(s,4096,(a,b)->{})==4096,"preflight exactsize");ByteArrayOutputStream output=new ByteArrayOutputStream();ImageContainers.Result result=ImageContainers.convert("dmgRaw",s,output,4096,(a,b)->{});yes(Arrays.equals(expected,output.toByteArray()),"independentPython expectedeverybyte");ImageContainers.verify("dmgRaw",source(output.toByteArray()),result,(a,b)->{});yes(true,"readbackSHA256");
 byte[] changed=output.toByteArray();changed[0]^=1;try{ImageContainers.verify("dmgRaw",source(changed),result,(a,b)->{});throw new AssertionError();}catch(IOException e){yes(e.getMessage().equals("VERIFY_FAILED"),"wrongoutputrejected");}
 fail("DMG_BOUNDS",input,4095);fail("DMG_HEADER",new byte[512],4096);byte[] bad=input.clone();bad[bad.length-512+63]=2;fail("DMG_UNSUPPORTED",bad,4096);bad=input.clone();bad[0]^=1;fail("DMG_CHECKSUM",bad,4096);bad=input.clone();bad[1030]^=127;fail("DMG_DATA",bad,4096);bad=input.clone();Arrays.fill(bad,bad.length-512+216,bad.length-512+224,(byte)0x7f);fail("DMG_BOUNDS",bad,4096);
 try{DmgConverter.budget(s,4096,(a,b)->{throw new IOException("CANCELLED");});throw new AssertionError();}catch(IOException e){yes(e.getMessage().equals("CANCELLED"),"cancel");}
 System.out.println("PASS: "+checks+" UDIF raw/zlib/zero/ignore conversion, independentPython every-byte, CRC/readback/bounds/cancel assertions");
 }
}
