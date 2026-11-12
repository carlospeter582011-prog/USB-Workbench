package org.usbworkbench;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Platform independent disk operations, tested on an in-memory block device. */
public final class DiskTools {
    public interface Progress { void update(long done,long total)throws IOException; }
    public static final class FatLayout {
        public final long total,partitionSectors,clusters,fatSectors;public final int clusterSectors;
        public FatLayout(long total,int requested)throws IOException{
            if(total<131072 || total>0xffffffffL)throw new IOException("FAT_SIZE");
            this.total=total;partitionSectors=total-2048;
            int spc=requested==0?(total<1048576?1:total<16777216?8:total<67108864?16:64):requested;
            if(spc<1||spc>64||(spc&(spc-1))!=0)throw new IOException("FAT_CLUSTER");
            long fat=1;
            // Monotone iteration from a conservative upper bound avoids size oscillation.
            fat=((partitionSectors/spc+2)*4+511)/512;
            long clusters=(partitionSectors-32-2*fat)/spc;
            if(clusters<65525||clusters>=0x0ffffff5L)throw new IOException("FAT_CLUSTER");
            this.fatSectors=fat;this.clusters=clusters;clusterSectors=spc;
        }
    }
    static void le16(byte[] b,int p,long v){b[p]=(byte)v;b[p+1]=(byte)(v>>8);}
    static void le32(byte[] b,int p,long v){for(int i=0;i<4;i++)b[p+i]=(byte)(v>>(8*i));}
    static void text(byte[] b,int p,String s){byte[] a=s.getBytes(StandardCharsets.US_ASCII);System.arraycopy(a,0,b,p,a.length);}
    public static void formatFat32(BlockDevice disk,String name,int cluster,Progress p)throws IOException{
        if(disk.blockSize()!=512)throw new IOException("FAT_SECTOR");
        if(name.trim().isEmpty()||!name.matches("[A-Za-z0-9 _-]{1,11}"))throw new IOException("FAT_LABEL");
        FatLayout l=new FatLayout(disk.blockCount(),cluster);
        String label=String.format(Locale.ROOT,"%-11s",name.toUpperCase(Locale.ROOT));
        // Invalidate MBR first; interrupted format must never look complete.
        disk.write(0,new byte[512],512);
        long end=2048+32+2*l.fatSectors+l.clusterSectors;
        byte[] zeros=new byte[16384];
        for(long s=1;s<end;){p.update(s,end);int n=(int)Math.min(32,end-s);disk.write(s,zeros,n*512);s+=n;}
        // Clear stale backup GPT at disk end; other data areas are not securely erased.
        for(long s=disk.blockCount()-33;s<disk.blockCount();s++)disk.write(s,new byte[512],512);
        byte[] boot=new byte[512];boot[0]=(byte)0xeb;boot[1]=0x58;boot[2]=(byte)0x90;text(boot,3,"USBWORK ");le16(boot,11,512);boot[13]=(byte)l.clusterSectors;le16(boot,14,32);boot[16]=2;boot[21]=(byte)0xf8;le16(boot,24,63);le16(boot,26,255);le32(boot,28,2048);le32(boot,32,l.partitionSectors);le32(boot,36,l.fatSectors);le32(boot,44,2);le16(boot,48,1);le16(boot,50,6);boot[64]=(byte)0x80;boot[66]=0x29;le32(boot,67,System.currentTimeMillis());text(boot,71,label);text(boot,82,"FAT32   ");boot[510]=0x55;boot[511]=(byte)0xaa;
        byte[] info=new byte[512];le32(info,0,0x41615252);le32(info,484,0x61417272);le32(info,488,l.clusters-1);le32(info,492,3);le32(info,508,0xaa550000L);
        disk.write(2048,boot,512);disk.write(2054,boot,512);disk.write(2049,info,512);disk.write(2055,info,512);
        byte[] fat=new byte[512];le32(fat,0,0x0ffffff8);le32(fat,4,0xffffffffL);le32(fat,8,0x0fffffff);
        disk.write(2080,fat,512);disk.write(2080+l.fatSectors,fat,512);
        byte[] root=new byte[512];text(root,0,label);root[11]=8;disk.write(2080+2*l.fatSectors,root,512);
        byte[] mbr=new byte[512];mbr[447]=(byte)0xfe;mbr[448]=(byte)0xff;mbr[449]=(byte)0xff;mbr[450]=0x0c;mbr[451]=(byte)0xfe;mbr[452]=(byte)0xff;mbr[453]=(byte)0xff;le32(mbr,454,2048);le32(mbr,458,l.partitionSectors);mbr[510]=0x55;mbr[511]=(byte)0xaa;
        p.update(end,end);disk.write(0,mbr,512);disk.sync();
        byte[] check=new byte[512];disk.read(0,check,512);if(!Arrays.equals(mbr,check))throw new IOException("VERIFY_FAILED");disk.read(2048,check,512);if(!Arrays.equals(boot,check))throw new IOException("VERIFY_FAILED");
    }
    public static void writeImage(BlockDevice disk,InputStream source,long size,Progress p)throws IOException{
        long capacity=disk.blockCount()*disk.blockSize();
        if(size<=0||size>capacity)throw new IOException("IMAGE_SIZE");
        byte[] buf=new byte[16384];long done=0;
        while(done<size){p.update(done,size);int n=(int)Math.min(buf.length,size-done);readFully(source,buf,n);int padded=(n+disk.blockSize()-1)/disk.blockSize()*disk.blockSize();Arrays.fill(buf,n,padded,(byte)0);disk.write(done/disk.blockSize(),buf,padded);done+=n;}
        disk.sync();p.update(done,size);
    }
    public static void verifyImage(BlockDevice disk,InputStream source,long size,Progress p)throws IOException{
        if(size<=0||size>disk.blockCount()*disk.blockSize())throw new IOException("IMAGE_SIZE");
        byte[] expected=new byte[16384],actual=new byte[16384];long done=0;
        while(done<size){p.update(done,size);int n=(int)Math.min(expected.length,size-done);readFully(source,expected,n);int padded=(n+disk.blockSize()-1)/disk.blockSize()*disk.blockSize();disk.read(done/disk.blockSize(),actual,padded);for(int i=0;i<n;i++)if(expected[i]!=actual[i])throw new IOException("VERIFY_FAILED @ "+(done+i));done+=n;}p.update(done,size);
    }
    public static void backup(BlockDevice disk,OutputStream target,Progress p)throws IOException{
        byte[] buf=new byte[16384];long total=disk.blockCount()*disk.blockSize();
        for(long done=0;done<total;){p.update(done,total);int n=(int)Math.min(buf.length,total-done);disk.read(done/disk.blockSize(),buf,n);target.write(buf,0,n);done+=n;}target.flush();p.update(total,total);
    }
    public static void wipe(BlockDevice disk,Progress p)throws IOException{wipe(disk,p,p);}
    public static void wipe(BlockDevice disk,Progress p,Progress verify)throws IOException{
        byte[] zero=new byte[16384];long total=disk.blockCount()*disk.blockSize();
        for(long done=0;done<total;){p.update(done,total);int n=(int)Math.min(zero.length,total-done);disk.write(done/disk.blockSize(),zero,n);done+=n;}disk.sync();p.update(total,total);verifyZero(disk,verify);
    }
    public static long verifyZero(BlockDevice disk,Progress p)throws IOException{
        int sector=disk.blockSize();long blocks=disk.blockCount();if(sector<1||sector>65536||blocks<1||blocks>Long.MAX_VALUE/sector)throw new IOException("DISK_GEOMETRY");byte[] data=new byte[sector*Math.max(1,16384/sector)];long total=blocks*sector;
        for(long done=0;done<total;){p.update(done,total);int n=(int)Math.min(data.length,total-done);disk.read(done/sector,data,n);for(int i=0;i<n;i++)if(data[i]!=0)throw new IOException("ZERO_MISMATCH LBA="+((done+i)/sector)+" BYTE="+((done+i)%sector));done+=n;}p.update(total,total);return total;
    }
    public static void readFully(InputStream in,byte[] data,int n)throws IOException{
        int done=0;while(done<n){int read=in.read(data,done,n-done);if(read<0)throw new EOFException("SOURCE_SHORT");if(read==0)throw new IOException("SOURCE_STALLED");done+=read;}
    }
    public static String sha256(InputStream in,long total,Progress p)throws IOException{
        try{MessageDigest hash=MessageDigest.getInstance("SHA-256");byte[] b=new byte[65536];long done=0;int n;while((n=in.read(b))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");hash.update(b,0,n);done+=n;p.update(done,total);}StringBuilder out=new StringBuilder();for(byte v:hash.digest())out.append(String.format(Locale.ROOT,"%02x",v));return out.toString();}catch(NoSuchAlgorithmException e){throw new IOException(e);}
    }
}
