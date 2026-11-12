package org.usbworkbench;
import java.io.*;import java.util.*;
/** NTFS metadata write combining. Engine reads see staged bytes; every flush is physically read back. */
public final class VerifiedWriteBuffer implements BlockDevice {
 private final BlockDevice disk;private final NativeTools.Cancelled cancelled;private final byte[] buffer=new byte[65536],check=new byte[65536];private long base;private int used;private boolean closed,failed;
 public VerifiedWriteBuffer(BlockDevice d,NativeTools.Cancelled c)throws IOException{disk=d;cancelled=c;if(d.blockSize()!=512&&d.blockSize()!=4096)throw new IOException("FAT_SECTOR");}
 public int blockSize(){return disk.blockSize();}public long blockCount(){return disk.blockCount();}
 private void ready()throws IOException{if(closed||failed)throw new IOException("ENGINE_BUFFER_CLOSED");if(cancelled.get()||Thread.currentThread().isInterrupted())throw new IOException("CANCELLED");}
 private void bounds(long at,byte[] b,int n)throws IOException{if(at<0||n<=0||n>b.length||n%blockSize()!=0||at>blockCount()||n/blockSize()>blockCount()-at)throw new IOException("DISK_BOUNDS");}
 public void read(long at,byte[] b,int n)throws IOException{ready();bounds(at,b,n);long offset=at-base;if(used>0&&offset>=0&&offset<=used/blockSize()&&n<=used-offset*blockSize()){System.arraycopy(buffer,(int)(offset*blockSize()),b,0,n);return;}flush();disk.read(at,b,n);}
 public void write(long at,byte[] b,int n)throws IOException{ready();bounds(at,b,n);int done=0;while(done<n){ready();long next=at+done/blockSize();if(used>0&&(next<base||next-base>used/blockSize()||next-base>=buffer.length/blockSize()))flush();if(used==0)base=next;int offset=(int)((next-base)*blockSize()),take=Math.min(n-done,buffer.length-offset);System.arraycopy(b,done,buffer,offset,take);used=Math.max(used,offset+take);done+=take;if(used==buffer.length)flush();}}
 private void flush()throws IOException{ready();if(used==0)return;try{disk.write(base,buffer,used);disk.read(base,check,used);for(int i=0;i<used;i++)if(buffer[i]!=check[i])throw new IOException("VERIFY_FAILED");used=0;}catch(IOException e){failed=true;used=0;throw e;}}
 public void sync()throws IOException{flush();try{disk.sync();}catch(IOException e){failed=true;throw e;}}
 /** Success-only barrier before the formatter's metadata checker/table commit. */
 public void finish()throws IOException{sync();}
 /** Discards uncommitted bytes on failure/cancellation. Never retries writes or closes the owner's device. */
 public void close(){used=0;closed=true;Arrays.fill(buffer,(byte)0);Arrays.fill(check,(byte)0);}
}
