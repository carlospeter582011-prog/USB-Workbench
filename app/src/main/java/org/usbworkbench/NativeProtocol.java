package org.usbworkbench;
import java.io.*;
/** Bounded native child I/O protocol. Rejects writes in inspection mode before any device call. */
public final class NativeProtocol {
    private static long u64(byte[] b,int at)throws IOException{long value=0;for(int i=0;i<8;i++)value|=(b[at+i]&255L)<<(i*8);if(value<0)throw new IOException("DISK_BOUNDS");return value;}
    private static int u32(byte[] b,int at)throws IOException{long n=0;for(int i=0;i<4;i++)n|=(b[at+i]&255L)<<(i*8);if(n>1048576)throw new IOException("ENGINE_PROTOCOL");return (int)n;}
    public static void serve(InputStream requests,OutputStream responses,BlockDevice disk,boolean writable,DiskTools.Progress progress)throws IOException{
        byte[] header=new byte[17];long processed=0;
        while(true){int first=requests.read();if(first<0)return;header[0]=(byte)first;byte[] rest=new byte[16];DiskTools.readFully(requests,rest,16);System.arraycopy(rest,0,header,1,16);
            if(header[0]!=0x55||header[1]!=0x42||header[2]!=0x57||header[3]!=0x31)throw new IOException("ENGINE_PROTOCOL");
            int op=header[4]&255,n=u32(header,13);long at=u64(header,5),capacity=disk.blockCount()*disk.blockSize();
            if(at>capacity||n>capacity-at)throw new IOException("DISK_BOUNDS");if(op!=1&&op!=2&&op!=3)throw new IOException("ENGINE_PROTOCOL");if(op==3&&(n!=0||at!=0))throw new IOException("ENGINE_PROTOCOL");
            if(op==2&&!writable)throw new IOException("ENGINE_READ_ONLY");progress.update(processed,-1);
            byte[] bytes=new byte[n];
            if(op==2){DiskTools.readFully(requests,bytes,n);writeBytes(disk,at,bytes);byte[] check=new byte[n];readBytes(disk,at,check);if(!java.util.Arrays.equals(bytes,check))throw new IOException("VERIFY_FAILED");}
            else if(op==1)readBytes(disk,at,bytes);
            else if(writable)disk.sync();
            byte[] response=new byte[8];for(int i=0;i<4;i++)response[i+4]=(byte)(n>>>(i*8));responses.write(response);if(op==1)responses.write(bytes);responses.flush();processed+=n;progress.update(processed,-1);
        }
    }
    public static void readBytes(BlockDevice disk,long offset,byte[] out)throws IOException{
        int sector=disk.blockSize(),done=0;byte[] block=new byte[16384];
        while(done<out.length){long at=offset+done;int skip=(int)(at%sector),n=Math.min(out.length-done,block.length-skip),rounded=(skip+n+sector-1)/sector*sector;disk.read(at/sector,block,rounded);System.arraycopy(block,skip,out,done,n);done+=n;}
    }
    public static void writeBytes(BlockDevice disk,long offset,byte[] in)throws IOException{
        int sector=disk.blockSize(),done=0;byte[] block=new byte[16384];
        while(done<in.length){long at=offset+done;int skip=(int)(at%sector),n=Math.min(in.length-done,block.length-skip),rounded=(skip+n+sector-1)/sector*sector;if(skip!=0||n%sector!=0)disk.read(at/sector,block,rounded);System.arraycopy(in,done,block,skip,n);disk.write(at/sector,block,rounded);done+=n;}
    }
}
