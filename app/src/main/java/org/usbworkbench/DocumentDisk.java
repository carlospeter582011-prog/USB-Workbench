package org.usbworkbench;
import android.content.Context;import android.net.Uri;import android.os.ParcelFileDescriptor;import android.system.*;import java.io.*;
/** A new user-created image document, never an internal raw disk or existing user file. */
final class DocumentDisk implements BlockDevice {
 private final ParcelFileDescriptor fd;private final Closeable tracked;private final long bytes;private final Context context;private final Uri uri;private long sinceCheck;
 DocumentDisk(Context c,Uri uri,long size,IoRegistry registry)throws IOException {if(size<33554432||size%512!=0)throw new IOException("IMAGE_SIZE");StorageGuard.require(c,uri,size);context=c;this.uri=uri;fd=c.getContentResolver().openFileDescriptor(uri,"rw");if(fd==null)throw new IOException("TARGET_OPEN");tracked=registry.track(fd);bytes=size;try{if(Os.fstat(fd.getFileDescriptor()).st_size!=0)throw new IOException("FILE_EXISTS");Os.ftruncate(fd.getFileDescriptor(),size);}catch(ErrnoException e){close();throw new IOException("TARGET_SEEK_REQUIRED",e);}catch(IOException e){close();throw e;}}
 public int blockSize(){return 512;}public long blockCount(){return bytes/512;}
 private void bounds(long at,int n)throws IOException{if(at<0||n<0||n%512!=0||at>bytes/512||n/512>bytes/512-at)throw new IOException("DISK_BOUNDS");}
 public void read(long at,byte[] b,int n)throws IOException{bounds(at,n);try{int done=0;while(done<n){int count=Os.pread(fd.getFileDescriptor(),b,done,n-done,at*512+done);if(count<=0)throw new IOException("SOURCE_SHORT");done+=count;}}catch(ErrnoException e){throw new IOException("TARGET_IO",e);}}
 public void write(long at,byte[] b,int n)throws IOException{bounds(at,n);if(sinceCheck>=4194304){StorageGuard.require(context,uri,n);sinceCheck=0;}sinceCheck+=n;try{int done=0;while(done<n){int count=Os.pwrite(fd.getFileDescriptor(),b,done,n-done,at*512+done);if(count<=0)throw new IOException("TARGET_STALLED");done+=count;}}catch(ErrnoException e){throw new IOException("TARGET_IO",e);}}
 public void sync()throws IOException{try{Os.fsync(fd.getFileDescriptor());}catch(ErrnoException e){throw new IOException("TARGET_IO",e);}}
 public void close(){try{tracked.close();}catch(IOException ignored){}}
}
