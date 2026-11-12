package org.usbworkbench;
import java.io.*;
/** Selects a read-only filesystem view from a user image. Never opens a real mount/device path. */
public final class FsImageTools {
 public static final class View{public final String kind;public final OpticalImage.Source source;View(String k,OpticalImage.Source s){kind=k;source=s;}}
 public static View open(OpticalImage.Source source,int partition)throws IOException{
  final long sourceSize=source.size();if(sourceSize%512!=0)throw new IOException("FS_GEOMETRY");BlockDevice disk=new BlockDevice(){public int blockSize(){return 512;}public long blockCount(){return sourceSize/512;}public void read(long at,byte[] b,int n)throws IOException{if(at<0||n<0||at>blockCount()||n>(blockCount()-at)*512)throw new IOException("FS_PARTITION");source.read(at*512,b,0,n);}public void write(long at,byte[] b,int n)throws IOException{throw new IOException("READ_ONLY");}public void sync(){}public void close(){}};
  FilesystemTools.Volume v=FilesystemTools.select(disk,partition,false);if(!v.kind.equals("ntfs")&&!v.kind.equals("ext"))throw new IOException("FSIMAGE_KIND");if(v.kind.equals("ntfs"))NtfsInspector.inspect(v.disk);
  return new View(v.kind,new OpticalImage.Source(){public long size(){return v.sectors*512;}public void read(long at,byte[] b,int off,int n)throws IOException{if(at<0||at>size()||n<0||n>size()-at)throw new IOException("FS_PARTITION");source.read(v.start*512+at,b,off,n);}public void close(){}});
 }
}
