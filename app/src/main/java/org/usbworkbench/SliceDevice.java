package org.usbworkbench;
import java.io.*;
/** Bounded partition view. The underlying whole drive remains owned by the caller. */
public final class SliceDevice implements BlockDevice {
    private final BlockDevice parent;private final long start,count;
    public SliceDevice(BlockDevice parent,long start,long count)throws IOException{if(start<0||count<1||start>parent.blockCount()||count>parent.blockCount()-start)throw new IOException("DISK_BOUNDS");this.parent=parent;this.start=start;this.count=count;}
    public int blockSize(){return parent.blockSize();}public long blockCount(){return count;}
    private void bounds(long at,int n)throws IOException{if(at<0||n<0||n%blockSize()!=0||at>count||n/blockSize()>count-at)throw new IOException("DISK_BOUNDS");}
    public void read(long at,byte[] b,int n)throws IOException{bounds(at,n);parent.read(start+at,b,n);}public void write(long at,byte[] b,int n)throws IOException{bounds(at,n);parent.write(start+at,b,n);}public void sync()throws IOException{parent.sync();}public void close(){}
}
