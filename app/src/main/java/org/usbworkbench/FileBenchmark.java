package org.usbworkbench;
import java.io.*;import java.util.*;
/** Writes only a newly-created temporary file, verifies every byte, never overwrites files. */
public final class FileBenchmark {
 public interface File {OutputStream output()throws IOException;InputStream input()throws IOException;void sync()throws IOException;}
 public static String run(File file,int mebibytes,DiskTools.Progress p)throws IOException {
  if(mebibytes<1||mebibytes>1024)throw new IOException("BENCH_SIZE");long total=mebibytes*1048576L;byte[] expected=new byte[65536],actual=new byte[65536];Random random=new Random(812437);random.nextBytes(expected);long start=System.nanoTime();
  try(OutputStream out=file.output()){for(long done=0;done<total;done+=expected.length){p.update(done,total*2);out.write(expected);}out.flush();}file.sync();double write=(System.nanoTime()-start)/1e9;start=System.nanoTime();
  try(InputStream in=file.input()){for(long done=0;done<total;done+=actual.length){p.update(total+done,total*2);DiskTools.readFully(in,actual,actual.length);if(!Arrays.equals(expected,actual))throw new IOException("VERIFY_FAILED");}if(in.read()!=-1)throw new IOException("SOURCE_CHANGED");}double read=(System.nanoTime()-start)/1e9;p.update(total*2,total*2);
  return String.format(Locale.ROOT,"Temporary file benchmark\nSize: %d MiB\nWrite: %.2f MiB/s\nRead: %.2f MiB/s\nFull byte comparison: PASS\nProvider and OS caching affect these measurements.",mebibytes,mebibytes/Math.max(write,0.000001),mebibytes/Math.max(read,0.000001));
 }
}
