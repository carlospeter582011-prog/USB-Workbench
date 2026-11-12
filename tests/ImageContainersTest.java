package org.usbworkbench;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class ImageContainersTest {
 static int checks;static final DiskTools.Progress QUIET=(a,b)->{};
 static class Source implements OpticalImage.Source{final byte[] bytes;Source(byte[] b){bytes=b;}public long size(){return bytes.length;}public void read(long at,byte[] b,int off,int n)throws IOException{if(at<0||n>bytes.length-at)throw new IOException("BOUNDS");System.arraycopy(bytes,(int)at,b,off,n);}public void close(){}}
 static void yes(boolean test){if(!test)throw new AssertionError();checks++;}
 interface Throwing{void run()throws Exception;}static void fail(Throwing task,String code)throws Exception{try{task.run();throw new AssertionError("Expected "+code);}catch(IOException e){if(!code.isEmpty()&&!e.getMessage().startsWith(code))throw e;checks++;}}
 static byte[] convert(String mode,byte[] data,long limit)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();ImageContainers.Result result=ImageContainers.convert(mode,new Source(data),out,limit,QUIET);byte[] saved=out.toByteArray();ImageContainers.verify(mode,new Source(saved),result,QUIET);checks++;return saved;}
 static void put(byte[] b,int at,long n,int count){for(int i=0;i<count;i++)b[at+i]=(byte)(n>>>(8*i));}
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args[0]);byte[] iso=Files.readAllBytes(root.resolve("original.iso"));long max=ImageContainers.MAX;
  byte[] zso=convert("isoZso",iso,max);Files.write(root.resolve("java.zso"),zso);yes(zso.length<iso.length);yes(Arrays.equals(iso,convert("zsoIso",zso,max)));for(int shift:new int[]{0,4})yes(Arrays.equals(iso,convert("zsoIso",Files.readAllBytes(root.resolve("python-"+shift+".zso")),max)));
  java.util.Random random=new java.util.Random(883);Lz4Block.Encoder encoder=new Lz4Block.Encoder();for(int i=0;i<500;i++){byte[] input=new byte[2048],packed=new byte[4096],output=new byte[2048];if(i%3==0)random.nextBytes(input);else for(int j=0;j<input.length;j++)input[j]=(byte)(j%(i%127+1));int n=encoder.encode(input,packed);yes(Lz4Block.decode(packed,n,output)==n&&Arrays.equals(input,output));}
  fail(()->Lz4Block.decode(new byte[]{0,0,0},3,new byte[2048]),"ZSO_DATA");fail(()->Lz4Block.decode(new byte[]{(byte)255,(byte)255},2,new byte[2048]),"ZSO_DATA");byte[] badZso=zso.clone();badZso[20]=2;fail(()->convert("zsoIso",badZso,max),"CSO_UNSUPPORTED");
  byte[] cso=convert("isoCso",iso,max);Files.write(root.resolve("java.cso"),cso);yes(cso.length<iso.length);yes(Arrays.equals(iso,convert("csoIso",cso,max)));
  yes(Arrays.equals(iso,convert("csoIso",Files.readAllBytes(root.resolve("python.cso")),max)));
  yes(Arrays.equals(iso,convert("gzipUnpack",Files.readAllBytes(root.resolve("python.img.gz")),max)));
  yes(Arrays.equals(iso,convert("xzUnpack",Files.readAllBytes(root.resolve("python.img.xz")),max)));
  byte[] gzip=convert("gzipPack",iso,max);Files.write(root.resolve("java.img.gz"),gzip);yes(Arrays.equals(iso,convert("gzipUnpack",gzip,max)));
  yes(ImageContainers.budget("gzipUnpack",new Source(gzip),max,QUIET)==iso.length);
  yes(ImageContainers.budget("isoCso",new Source(iso),max,QUIET)>=cso.length);
  yes(ImageContainers.budget("csoIso",new Source(cso),max,QUIET)==iso.length);
  fail(()->ImageContainers.budget("gzipUnpack",new Source(gzip),iso.length-1,QUIET),"CONVERT_LIMIT");
  fail(()->convert("csoIso",cso,iso.length-1),"CONVERT_LIMIT");
  byte[] shortFile=Arrays.copyOf(cso,30);fail(()->convert("csoIso",shortFile,max),"CSO_INDEX");
  for(int offset:new int[]{20,21,16}){byte[] bad=cso.clone();bad[offset]=99;fail(()->convert("csoIso",bad,max),"CSO_UNSUPPORTED");}
  byte[] overlap=cso.clone();put(overlap,24,0,4);fail(()->convert("csoIso",overlap,max),"CSO_INDEX");
  byte[] backwards=cso.clone();put(backwards,28,0,4);fail(()->convert("csoIso",backwards,max),"CSO_INDEX");
  byte[] negative=cso.clone();negative[15]=(byte)128;fail(()->convert("csoIso",negative,max),"CSO_HEADER");
  byte[] excessive=cso.clone();put(excessive,8,Long.MAX_VALUE,8);fail(()->convert("csoIso",excessive,max),"CONVERT_LIMIT");
  byte[] corrupt=gzip.clone();corrupt[corrupt.length-8]^=1;fail(()->convert("gzipUnpack",corrupt,max),"");
  byte[] corruptXz=Files.readAllBytes(root.resolve("python.img.xz"));corruptXz[corruptXz.length-16]^=1;fail(()->convert("xzUnpack",corruptXz,max),"");
  fail(()->convert("gzipUnpack",Arrays.copyOf(gzip,gzip.length-5),max),"");
  fail(()->convert("xzUnpack",gzip,max),"");
  fail(()->convert("isoCso",new byte[2048],max),"CSO_ISO");
  fail(()->ImageContainers.convert("isoCso",new Source(iso),new ByteArrayOutputStream(),max,(a,b)->{throw new IOException("CANCELLED");}),"CANCELLED");
  Source changing=new Source(iso){int passes;public void read(long at,byte[] b,int off,int n)throws IOException{super.read(at,b,off,n);if(at==0)passes++;if(passes>=2&&at==0)b[off]^=1;}};
  fail(()->ImageContainers.convert("isoCso",changing,new ByteArrayOutputStream(),max,QUIET),"SOURCE_CHANGED");
  ByteArrayOutputStream raw=new ByteArrayOutputStream();ImageContainers.Result r=ImageContainers.convert("gzipUnpack",new Source(gzip),raw,max,QUIET);byte[] badRaw=raw.toByteArray();badRaw[0]^=1;fail(()->ImageContainers.verify("gzipUnpack",new Source(badRaw),r,QUIET),"VERIFY_FAILED");
  yes(!ImageContainers.supported("zso"));yes(ReportText.render("CONTAINER_OK image.iso\n12\nSHA-256: abc",true).contains("بصمة SHA-256"));yes(!ReportText.render("CONTAINER_OK image.iso\n12\nSHA-256: abc",true).contains("Original data"));
  String splitText="CONSOLE_SPLIT_OK\n2\nقرص.iso.usbwb\nabc";yes(ReportText.render(splitText,false).contains("Part count: 2"));yes(ReportText.render(splitText,true).contains("قرص.iso.usbwb")&&!ReportText.render(splitText,true).contains("Part count"));String joined="CONSOLE_JOIN_OK\nملف.bin\nabc";yes(ReportText.render(joined,false).contains("ملف.bin"));yes(ReportText.render(joined,true).contains("ملف.bin")&&!ReportText.render(joined,true).contains("Parts joined"));
  System.out.println("PASS: "+checks+" image container conversion, independent fixtures, malformed headers/index/CRC, limits, cancellation, source-change, readback and language checks");
 }
}
