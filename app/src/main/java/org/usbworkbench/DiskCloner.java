package org.usbworkbench;
import java.io.*;
import java.util.Arrays;
/** Full write followed by full source/target comparison; equal geometry preserves GPT location. */
public final class DiskCloner {
    public static void clone(BlockDevice source,BlockDevice target,DiskTools.Progress progress)throws IOException{
        if(source==target)throw new IOException("CLONE_SAME");if(source.blockSize()!=target.blockSize()||source.blockCount()!=target.blockCount())throw new IOException("CLONE_GEOMETRY");
        long size=source.blockCount()*source.blockSize();byte[] a=new byte[16384],b=new byte[16384];
        for(long done=0;done<size;){progress.update(done,size*2);int n=(int)Math.min(a.length,size-done);source.read(done/source.blockSize(),a,n);target.write(done/target.blockSize(),a,n);done+=n;}target.sync();
        for(long done=0;done<size;){progress.update(size+done,size*2);int n=(int)Math.min(a.length,size-done);source.read(done/source.blockSize(),a,n);target.read(done/target.blockSize(),b,n);for(int i=0;i<n;i++)if(a[i]!=b[i])throw new IOException("VERIFY_FAILED @ "+(done+i));done+=n;}progress.update(size*2,size*2);
    }
}
