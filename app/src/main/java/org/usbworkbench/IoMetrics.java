package org.usbworkbench;
import java.io.IOException;
/** Physical block IO counters for format diagnostics; no access outside explicitly wrapped operations. */
public final class IoMetrics {
 public volatile long readBytes,writeBytes,reads,writes,syncs;
 public volatile String action="",lastAction="";private volatile long began,lastFinished,stopped;private final long created=System.nanoTime();
 private void begin(String value){began=System.nanoTime();lastAction=action=value;}
 private void end(){lastFinished=System.nanoTime();action="";}
 private long now(){return stopped==0?System.nanoTime():stopped;}
 public void freeze(){if(stopped==0)stopped=System.nanoTime();}
 public long activeMillis(){return action.isEmpty()?0:Math.max(0,(now()-began)/1000000);}
 public long idleMillis(){return Math.max(0,(now()-(lastFinished==0?created:lastFinished))/1000000);}
 public BlockDevice wrap(BlockDevice disk){return new BlockDevice(){
  public int blockSize(){return disk.blockSize();}public long blockCount(){return disk.blockCount();}
  public void read(long at,byte[] b,int n)throws IOException{begin("read");try{disk.read(at,b,n);readBytes+=n;reads++;}finally{end();}}
  public void write(long at,byte[] b,int n)throws IOException{begin("write");try{disk.write(at,b,n);writeBytes+=n;writes++;}finally{end();}}
  public void sync()throws IOException{begin("sync");try{disk.sync();syncs++;}finally{end();}}
  public void close(){} // Caller owns the physical USB connection.
 };}
}
