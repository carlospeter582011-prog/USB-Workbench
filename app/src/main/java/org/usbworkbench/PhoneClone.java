package org.usbworkbench;
import java.io.*;
/** Complete USB image to a user-created phone file, followed by full byte comparison. */
public final class PhoneClone {
 public interface Image {OutputStream output(long size)throws IOException;InputStream input()throws IOException;}
 public static void clone(BlockDevice source,Image image,DiskTools.Progress p)throws IOException{
  long size=source.blockCount()*source.blockSize();if(size<1||size>Long.MAX_VALUE/2)throw new IOException("DISK_BOUNDS");
  try(OutputStream out=image.output(size)){DiskTools.backup(source,out,(done,total)->p.update(done,size*2));}
  try(InputStream in=image.input()){DiskTools.verifyImage(source,in,size,(done,total)->p.update(size+done,size*2));}
  p.update(size*2,size*2);
 }
}
