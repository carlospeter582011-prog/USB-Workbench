package org.usbworkbench;
import java.io.*;import java.security.*;import java.util.*;import java.util.zip.*;import org.tukaani.xz.XZInputStream;
/** Original streaming container tools. No filesystem/USB writes, no imported executable code. */
public final class ImageContainers {
 public static final long MAX=16L*1024*1024*1024;
 public static boolean supported(String mode){return Arrays.asList("isoCso","csoIso","isoZso","zsoIso","gzipPack","gzipUnpack","xzUnpack","dmgRaw").contains(mode);}
 public static final class Result {public final long bytes;public final byte[] digest;Result(long n,byte[] d){bytes=n;digest=d;}public String hash(){StringBuilder s=new StringBuilder();for(byte b:digest)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}}
 private static MessageDigest sha(){try{return MessageDigest.getInstance("SHA-256");}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
 private static void limit(long n,long max)throws IOException{if(n<1||n>max||max<1||max>MAX)throw new IOException("CONVERT_LIMIT");}
 public static long budget(String mode,OpticalImage.Source source,long max,DiskTools.Progress p)throws IOException {
  if(!supported(mode))throw new IOException("JOB_UNKNOWN");long n=source.size();if(n<1||n>MAX+MAX/32+65536)throw new IOException("CONVERT_LIMIT");
  if(mode.equals("dmgRaw"))return DmgConverter.budget(source,max,p);
  if(mode.equals("csoIso")||mode.equals("zsoIso")){long bytes=new Cso(source,p,mode.equals("zsoIso")).bytes;limit(bytes,max);return bytes;}
  if(mode.equals("isoCso")||mode.equals("isoZso")){iso(source);limit(n,max);return n+((n/2048)+1)*20+65536;}
  if(mode.equals("gzipPack")){limit(n,max);return n+n/1000+65536;}
  try(InputStream in=decoded(mode,source)){return copy(in,null,max,p).bytes;}
 }
 public static Result convert(String mode,OpticalImage.Source source,OutputStream output,long max,DiskTools.Progress p)throws IOException{
  if(!supported(mode))throw new IOException("JOB_UNKNOWN");
  if(mode.equals("dmgRaw"))return DmgConverter.convert(source,output,max,p);
  if(mode.equals("isoCso")||mode.equals("isoZso"))return packCso(source,output,max,p,mode.equals("isoZso"));
  if(mode.equals("gzipPack")){limit(source.size(),max);try(GZIPOutputStream gzip=new GZIPOutputStream(output,65536)){return copy(new SourceStream(source),gzip,max,p);}}
  if(mode.equals("csoIso")||mode.equals("zsoIso")){Cso cso=new Cso(source,p,mode.equals("zsoIso"));limit(cso.bytes,max);return copy(cso.stream(p),output,max,p);}
  try(InputStream in=decoded(mode,source)){return copy(in,output,max,p);}
 }
 public static void verify(String mode,OpticalImage.Source output,Result expected,DiskTools.Progress p)throws IOException{
  Result actual;
  if(mode.equals("isoCso")||mode.equals("isoZso")){Cso cso=new Cso(output,p,mode.equals("isoZso"));if(cso.bytes!=expected.bytes)throw new IOException("VERIFY_FAILED");actual=copy(cso.stream(p),null,expected.bytes,p);}
  else if(mode.equals("gzipPack")){try(InputStream in=decoded("gzipUnpack",output)){actual=copy(in,null,expected.bytes,p);}}
  else {if(output.size()!=expected.bytes)throw new IOException("VERIFY_FAILED");actual=copy(new SourceStream(output),null,expected.bytes,p);}
  if(actual.bytes!=expected.bytes||!MessageDigest.isEqual(actual.digest,expected.digest))throw new IOException("VERIFY_FAILED");
 }
 private static IOException compressed(IOException error){if(error instanceof org.tukaani.xz.MemoryLimitException)return new IOException("XZ_MEMORY_LIMIT",error);return new IOException("COMPRESSED_DATA",error);}
 private static InputStream decoded(String mode,OpticalImage.Source source)throws IOException{
  InputStream raw;try{if(mode.equals("gzipUnpack"))raw=new GZIPInputStream(new SourceStream(source),65536);else if(mode.equals("xzUnpack"))raw=new XZInputStream(new SourceStream(source),32768);else throw new IOException("JOB_UNKNOWN");}catch(IOException e){throw compressed(e);}
  return new FilterInputStream(raw){public int read()throws IOException{try{return in.read();}catch(IOException e){throw compressed(e);}}public int read(byte[] b,int off,int n)throws IOException{try{return in.read(b,off,n);}catch(IOException e){throw compressed(e);}}};
 }
 private static Result copy(InputStream in,OutputStream out,long max,DiskTools.Progress p)throws IOException{
  byte[] b=new byte[65536];long done=0;MessageDigest digest=sha();int n;
  while((n=in.read(b))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");if(n>max-done)throw new IOException("CONVERT_LIMIT");p.update(done,max);if(out!=null)out.write(b,0,n);digest.update(b,0,n);done+=n;}
  limit(done,max);if(out!=null)out.flush();p.update(done,done);return new Result(done,digest.digest());
 }
 private static final class SourceStream extends InputStream {final OpticalImage.Source source;long at;SourceStream(OpticalImage.Source s){source=s;}public int read()throws IOException{byte[] b=new byte[1];return read(b,0,1)<0?-1:b[0]&255;}public int read(byte[] b,int off,int n)throws IOException{if(n==0)return 0;long left=source.size()-at;if(left<=0)return -1;n=(int)Math.min(n,left);source.read(at,b,off,n);at+=n;return n;}public int available()throws IOException{return (int)Math.min(Integer.MAX_VALUE,source.size()-at);}}
 private static long le(byte[] b,int at,int n)throws IOException{long v=0;for(int i=0;i<n;i++)v|=(b[at+i]&255L)<<(8*i);if(v<0)throw new IOException("CSO_HEADER");return v;}
 private static void put(byte[] b,int at,long v,int n){for(int i=0;i<n;i++)b[at+i]=(byte)(v>>>(8*i));}
 private static long aligned(long n){return (n+15)&~15L;}
 private static void iso(OpticalImage.Source source)throws IOException{long n=source.size();if(n<17*2048||n%2048!=0)throw new IOException("CSO_ISO");byte[] b=new byte[7];source.read(32768,b,0,7);if(b[0]!=1||b[1]!='C'||b[2]!='D'||b[3]!='0'||b[4]!='0'||b[5]!='1'||b[6]!=1)throw new IOException("CSO_ISO");}
 private static int compress(Deflater d,byte[] raw,byte[] packed)throws IOException{d.reset();d.setInput(raw);d.finish();int n=d.deflate(packed);if(!d.finished())throw new IOException("CSO_DEFLATE");return n;}
 private static Result packCso(OpticalImage.Source source,OutputStream out,long max,DiskTools.Progress p,boolean zso)throws IOException{
  iso(source);long size=source.size();limit(size,max);long blocks=size/2048,start=aligned(24+4*(blocks+1)),at=start;byte[] header=new byte[24];header[0]=(byte)(zso?'Z':'C');header[1]='I';header[2]='S';header[3]='O';put(header,4,24,4);put(header,8,size,8);put(header,16,2048,4);header[20]=1;header[21]=4;out.write(header);
  byte[] raw=new byte[2048],packed=new byte[4096],entry=new byte[4],zero=new byte[16];MessageDigest before=sha(),after=sha();Deflater d=new Deflater(6,true);Lz4Block.Encoder lz4=zso?new Lz4Block.Encoder():null;
  try{
   // Build only the index on pass one; never allocate an image-sized index or temporary image.
   for(long i=0;i<blocks;i++){p.update(i*2048,size*3);source.read(i*2048,raw,0,2048);before.update(raw);int n=zso?lz4.encode(raw,packed):compress(d,raw,packed);put(entry,0,(at>>>4)|(n>=2048?0x80000000L:0),4);out.write(entry);at=aligned(at+Math.min(n,2048));}
   put(entry,0,at>>>4,4);out.write(entry);out.write(zero,0,(int)(start-(24+4*(blocks+1))));
   for(long i=0;i<blocks;i++){p.update(size+i*2048,size*3);source.read(i*2048,raw,0,2048);after.update(raw);int n=zso?lz4.encode(raw,packed):compress(d,raw,packed);n=Math.min(n,2048);out.write(n==2048?raw:packed,0,n);out.write(zero,0,(int)(aligned(n)-n));}
   byte[] expected=before.digest(),written=after.digest();if(source.size()!=size||!MessageDigest.isEqual(expected,written))throw new IOException("SOURCE_CHANGED");out.flush();return new Result(size,written);
  }finally{d.end();}
 }
 private static final class Index {final OpticalImage.Source source;final byte[] cache=new byte[65536];long base=-1;int count;Index(OpticalImage.Source s){source=s;}long get(long i)throws IOException{long at=24+4*i;if(base<0||at<base||at+4>base+count){base=at;count=(int)Math.min(cache.length,source.size()-at);if(count<4)throw new IOException("CSO_INDEX");source.read(base,cache,0,count);}return le(cache,(int)(at-base),4);}}
 private static final class Cso {
  final OpticalImage.Source source;final Index index;final long bytes,blocks;final int shift,alignment;final boolean zso;
  Cso(OpticalImage.Source s,DiskTools.Progress p,boolean zso)throws IOException{source=s;this.zso=zso;if(s.size()<28)throw new IOException("CSO_HEADER");byte[] h=new byte[24];s.read(0,h,0,24);if(h[0]!=(zso?'Z':'C')||h[1]!='I'||h[2]!='S'||h[3]!='O'||(h[20]&255)!=1||le(h,16,4)!=2048||(le(h,4,4)!=0&&le(h,4,4)!=24))throw new IOException("CSO_UNSUPPORTED");bytes=le(h,8,8);limit(bytes,MAX);if(bytes%2048!=0)throw new IOException("CSO_HEADER");blocks=bytes/2048;shift=h[21]&255;if(shift>12)throw new IOException("CSO_UNSUPPORTED");alignment=1<<shift;long end=24+(blocks+1)*4;if(end>s.size())throw new IOException("CSO_INDEX");index=new Index(s);long prev=index.get(0),offset=(prev&0x7fffffffL)<<shift;if(offset<end)throw new IOException("CSO_INDEX");
   for(long i=0;i<blocks;i++){if((i&4095)==0)p.update(i*2048,bytes);long next=index.get(i+1),finish=(next&0x7fffffffL)<<shift;if(zso&&i==blocks-1&&finish<=s.size()&&s.size()-finish<alignment)finish=s.size();long span=finish-offset;if(finish>s.size()||span<=0||span>4096+alignment||((prev&0x80000000L)!=0&&(span<2048||span>=2048+alignment)))throw new IOException("CSO_INDEX");offset=finish;prev=next;}
   if((prev&0x80000000L)!=0)throw new IOException("CSO_INDEX");
  }
  InputStream stream(DiskTools.Progress p){return new InputStream(){long block;int at=2048;final byte[] raw=new byte[2048],packed=new byte[8192];public int read()throws IOException{byte[] b=new byte[1];return read(b,0,1)<0?-1:b[0]&255;}public int read(byte[] b,int off,int n)throws IOException{if(n==0)return 0;if(at==2048){if(block==blocks)return -1;p.update(block*2048,bytes);long entry=index.get(block),offset=(entry&0x7fffffffL)<<shift;long finish=(index.get(block+1)&0x7fffffffL)<<shift;if(zso&&block==blocks-1&&finish<=source.size()&&source.size()-finish<alignment)finish=source.size();int span=(int)(finish-offset);if((entry&0x80000000L)!=0)source.read(offset,raw,0,2048);else{source.read(offset,packed,0,span);if(zso){int consumed=Lz4Block.decode(packed,span,raw);if(span-consumed>=alignment)throw new IOException("ZSO_DATA");}else{Inflater inflater=new Inflater(true);try{inflater.setInput(packed,0,span);int written=0;while(written<2048){int count=inflater.inflate(raw,written,2048-written);if(count==0)break;written+=count;}byte[] extra=new byte[1];int surplus=inflater.inflate(extra);if(written!=2048||surplus!=0||!inflater.finished()||inflater.getRemaining()>=alignment)throw new IOException("CSO_DATA");}catch(DataFormatException e){throw new IOException("CSO_DATA",e);}finally{inflater.end();}}}block++;at=0;}int count=Math.min(n,2048-at);System.arraycopy(raw,at,b,off,count);at+=count;return count;}};}
 }
}
