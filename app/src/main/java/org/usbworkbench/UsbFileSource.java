package org.usbworkbench;
import java.io.*;
/** Read-only byte view over sector IO. Small aligned cache; never copies the whole drive. */
public final class UsbFileSource implements OpticalImage.Source {
 private final BlockDevice disk;private final int sector;private final long bytes;private final byte[] cache;private long base=-1;private int count;
 public UsbFileSource(BlockDevice d)throws IOException{disk=d;sector=d.blockSize();if(sector!=512||d.blockCount()<1||d.blockCount()>Long.MAX_VALUE/sector)throw new IOException("FS_GEOMETRY");bytes=d.blockCount()*sector;cache=new byte[16384];}
 public long size(){return bytes;}
 public synchronized void read(long at,byte[] b,int off,int n)throws IOException{if(at<0||off<0||n<0||off>b.length-n||at>bytes||n>bytes-at)throw new IOException("FS_PARTITION");while(n>0){if(base<0||at<base||at>=base+count){base=(at/cache.length)*cache.length;count=(int)Math.min(cache.length,bytes-base);disk.read(base/sector,cache,count);}int start=(int)(at-base),take=Math.min(n,count-start);System.arraycopy(cache,start,b,off,take);at+=take;off+=take;n-=take;}}
 public synchronized void invalidate(){base=-1;count=0;}
 public void close(){}
}
