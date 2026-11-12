package org.usbworkbench;
import java.io.*;import java.util.*;
/** CHDv5 standalone HD/DVD logical-byte decoder. CD track reconstruction is deliberately not guessed. */
public final class ChdTools {
 public static final class Plan{public final long bytes,limit;public final int unit;public final String rawSha1,sourceHash;Plan(long b,int u,String sha,String hash,long l){bytes=b;unit=u;rawSha1=sha;sourceHash=hash;limit=l;}}
 private static final WimFiles.Store NO_OUTPUT=new WimFiles.Store(){public WimFiles.RandomFile create(String n)throws IOException{throw new IOException("CHD_OUTPUT");}public void capacity(long n){}};
 public static Plan inspect(File libs,OpticalImage.Source source,long limit,DiskTools.Progress p,ArchiveProcess.Cancelled cancel)throws IOException{
  if(source.size()<124||source.size()>64L*1024*1024*1024||limit<1||limit>ArchiveTools.MAX)throw new IOException("CONVERT_LIMIT");String hash=ArchiveTools.hash(source,p,cancel),log;
  try(ArchiveFiles files=new ArchiveFiles(source,NO_OUTPUT,Collections.emptyMap())){log=ArchiveProcess.chd(libs,Arrays.asList("check",Long.toString(limit)),files,p,cancel);}
  return parse(log,hash,limit);
 }
 private static Plan parse(String log,String hash,long limit)throws IOException{
  for(String line:log.split("\\n"))if(line.startsWith("CHD_INFO ")){String[] parts=line.split(" ");if(parts.length!=4||!parts[3].matches("[0-9a-f]{40}"))break;try{long bytes=Long.parseLong(parts[1]);int unit=Integer.parseInt(parts[2]);if(bytes<1||bytes>limit||(unit!=512&&unit!=2048)||bytes%unit!=0)break;return new Plan(bytes,unit,parts[3],hash,limit);}catch(NumberFormatException ignored){break;}}
  throw new IOException("CHD_METADATA");
 }
 public static String convert(File libs,OpticalImage.Source source,Plan plan,WimFiles.Store store,DiskTools.Progress p,ArchiveProcess.Cancelled cancel)throws IOException{
  String hash,log;try(ArchiveFiles files=new ArchiveFiles(source,store,Collections.singletonMap("export.bin",plan.bytes))){log=ArchiveProcess.chd(libs,Arrays.asList("convert",Long.toString(plan.limit)),files,p,cancel);if(!Long.valueOf(plan.bytes).equals(files.outputs().get("export.bin")))throw new IOException("CHD_METADATA");hash=files.hashes().get("export.bin");}
  Plan after=parse(log,plan.sourceHash,plan.limit);if(after.bytes!=plan.bytes||after.unit!=plan.unit||!after.rawSha1.equals(plan.rawSha1)||!ArchiveTools.hash(source,p,cancel).equals(plan.sourceHash))throw new IOException("SOURCE_CHANGED");return hash;
 }
 public static void verify(InputStream input,Plan plan,String expected,DiskTools.Progress p)throws IOException{
  try{java.security.MessageDigest sha256=java.security.MessageDigest.getInstance("SHA-256"),sha1=java.security.MessageDigest.getInstance("SHA-1");byte[] b=new byte[65536];long done=0;int n;while((n=input.read(b))!=-1){if(n==0)throw new IOException("TARGET_STALLED");if(n>plan.bytes-done)throw new IOException("VERIFY_FAILED");sha256.update(b,0,n);sha1.update(b,0,n);done+=n;p.update(done,plan.bytes);}if(done!=plan.bytes||!hex(sha256.digest()).equals(expected)||!plan.rawSha1.equals("0000000000000000000000000000000000000000")&&!hex(sha1.digest()).equals(plan.rawSha1))throw new IOException("VERIFY_FAILED");}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
 }
 private static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
}
