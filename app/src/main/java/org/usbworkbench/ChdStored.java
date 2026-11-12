package org.usbworkbench;
import java.io.*;import java.util.*;import java.security.*;
/** Original bounded CHDv5 stored-hunk writer. No compression, CD tracks, parent or boot claims. */
public final class ChdStored {
 public static final int HUNK=65536;
 public static long outputSize(long size,int unit,long limit)throws IOException{if((unit!=512&&unit!=2048)||size<unit||size%unit!=0||size>limit||limit<1||limit>16L*1073741824)throw new IOException("CHD_CREATE_OPTIONS");long hunks=(size+HUNK-1)/HUNK,data=(124+4*hunks+HUNK-1)/HUNK*HUNK;return data+hunks*HUNK;}
 private static MessageDigest digest(String kind){try{return MessageDigest.getInstance(kind);}catch(Exception e){throw new AssertionError(e);}}
 private static void be(byte[] b,int at,long n,int count){for(int i=count-1;i>=0;i--){b[at+i]=(byte)n;n>>>=8;}}
 public static String create(OpticalImage.Source source,WimFiles.RandomFile output,int unit,long limit,DiskTools.Progress p,ArchiveProcess.Cancelled cancel)throws IOException{
  long size=source.size(),total=outputSize(size,unit,limit),hunks=(size+HUNK-1)/HUNK,start=total-hunks*HUNK;if(output.size()!=0)throw new IOException("FILE_EXISTS");if(cancel.get())throw new IOException("CANCELLED");String original=ArchiveTools.hash(source,p,cancel);output.truncate(total);byte[] header=new byte[124];output.write(0,header);
  byte[] map=new byte[65536];for(long i=0;i<hunks;){if(cancel.get())throw new IOException("CANCELLED");int n=(int)Math.min(map.length/4,hunks-i);for(int j=0;j<n;j++)be(map,j*4,start/HUNK+i+j,4);output.write(124+i*4,Arrays.copyOf(map,n*4));i+=n;}
  byte[] buffer=new byte[HUNK];MessageDigest sha1=digest("SHA-1"),sha256=digest("SHA-256");long done=0;
  for(long i=0;i<hunks;i++){if(cancel.get())throw new IOException("CANCELLED");int n=(int)Math.min(HUNK,size-done);Arrays.fill(buffer,(byte)0);source.read(done,buffer,0,n);sha1.update(buffer,0,n);sha256.update(buffer,0,n);output.write(start+i*HUNK,buffer);byte[] check=new byte[HUNK];if(output.read(start+i*HUNK,check)!=HUNK||!Arrays.equals(buffer,check))throw new IOException("VERIFY_FAILED");done+=n;p.update(done,size);}
  String copied=hex(sha256.digest());if(!original.equals(copied)||source.size()!=size||!original.equals(ArchiveTools.hash(source,p,cancel)))throw new IOException("SOURCE_CHANGED");if(cancel.get())throw new IOException("CANCELLED");byte[] raw=sha1.digest();System.arraycopy("MComprHD".getBytes(java.nio.charset.StandardCharsets.US_ASCII),0,header,0,8);be(header,8,124,4);be(header,12,5,4);be(header,32,size,8);be(header,40,124,8);be(header,56,HUNK,4);be(header,60,unit,4);System.arraycopy(raw,0,header,64,20);byte[] overall=digest("SHA-1").digest(raw);System.arraycopy(overall,0,header,84,20);output.write(0,header);output.sync();byte[] check=new byte[124];if(output.read(0,check)!=124||!Arrays.equals(header,check)||output.size()!=total)throw new IOException("VERIFY_FAILED");return original;
 }
 /** Decoder verification sink keeps only the last bounded write; never another full raw image. */
 public static String verify(File libs,OpticalImage.Source saved,String rawHash,long limit,DiskTools.Progress p,ArchiveProcess.Cancelled cancel)throws IOException{
  ChdTools.Plan plan=ChdTools.inspect(libs,saved,limit,p,cancel);WimFiles.RandomFile window=new WimFiles.RandomFile(){long size,offset;byte[] last=new byte[0];public long size(){return size;}public int read(long at,byte[] b)throws IOException{if(at<offset||at+b.length>offset+last.length)throw new IOException("CHD_VERIFY_WINDOW");System.arraycopy(last,(int)(at-offset),b,0,b.length);return b.length;}public void write(long at,byte[] b)throws IOException{if(at!=size||b.length>1048576)throw new IOException("CHD_VERIFY_WINDOW");offset=at;last=b.clone();size+=b.length;}public void truncate(long n)throws IOException{throw new IOException("CHD_VERIFY_WINDOW");}public void sync(){}public void close(){}};
  String decoded=ChdTools.convert(libs,saved,plan,new WimFiles.Store(){public void capacity(long n){}public WimFiles.RandomFile create(String name){return window;}},p,cancel);if(!decoded.equals(rawHash))throw new IOException("VERIFY_FAILED");return decoded;
 }
 private static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format(Locale.ROOT,"%02x",x&255));return s.toString();}
}
