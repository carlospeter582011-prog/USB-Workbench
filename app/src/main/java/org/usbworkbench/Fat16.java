package org.usbworkbench;
import java.io.*;
import java.util.*;
public final class Fat16 {
    public static final class Layout {
        public final long volume,fat,clusters;public final int spc,bits;
        public Layout(long sectors,int choice)throws IOException{this(sectors,choice,16);}
        public Layout(long sectors,int choice,int bits)throws IOException{
            this.bits=bits;long max=bits==12?65536:4194304;int min=bits==12?4096:16384;
            if(sectors<min||sectors>max)throw new IOException(bits==12?"FAT12_SIZE":"FAT16_SIZE");volume=sectors-2048;
            int selected=choice;
            if(selected==0){selected=1;while(selected<64&&(volume-1-32)/selected>=(bits==12?4085:65525))selected*=2;}
            if(selected<1||selected>64||(selected&(selected-1))!=0)throw new IOException("FAT_CLUSTER");
            spc=selected;fat=(((volume/spc+2)*bits+7)/8+511)/512;clusters=(volume-1-32-2*fat)/spc;
            if(clusters<(bits==12?1:4085)||clusters>=(bits==12?4085:65525))throw new IOException("FAT_CLUSTER");
        }
    }
    public static void format(BlockDevice d,String name,int cluster,DiskTools.Progress p)throws IOException{
        format(d,name,cluster,16,p);
    }
    public static void format(BlockDevice d,String name,int cluster,int bits,DiskTools.Progress p)throws IOException{
        if(d.blockSize()!=512)throw new IOException("FAT_SECTOR");if(name.trim().isEmpty()||!name.matches("[A-Za-z0-9 _-]{1,11}"))throw new IOException("FAT_LABEL");Layout l=new Layout(d.blockCount(),cluster,bits);
        String label=String.format(java.util.Locale.ROOT,"%-11s",name.toUpperCase(java.util.Locale.ROOT));long end=2048+1+2*l.fat+32;
        p.update(0,end);d.write(0,new byte[512],512);byte[] zero=new byte[16384];for(long at=1;at<end;){p.update(at,end);int n=(int)Math.min(32,end-at);d.write(at,zero,n*512);at+=n;}
        for(long at=d.blockCount()-33;at<d.blockCount();at++)d.write(at,new byte[512],512);
        byte[] b=new byte[512];b[0]=(byte)0xeb;b[1]=0x3c;b[2]=(byte)0x90;DiskTools.text(b,3,"USBWORK ");DiskTools.le16(b,11,512);b[13]=(byte)l.spc;DiskTools.le16(b,14,1);b[16]=2;DiskTools.le16(b,17,512);if(l.volume<65536)DiskTools.le16(b,19,l.volume);else DiskTools.le32(b,32,l.volume);b[21]=(byte)0xf8;DiskTools.le16(b,22,l.fat);DiskTools.le16(b,24,63);DiskTools.le16(b,26,255);DiskTools.le32(b,28,2048);b[36]=(byte)0x80;b[38]=0x29;DiskTools.le32(b,39,System.currentTimeMillis());DiskTools.text(b,43,label);DiskTools.text(b,54,bits==12?"FAT12   ":"FAT16   ");b[510]=0x55;b[511]=(byte)0xaa;d.write(2048,b,512);
        byte[] fat=new byte[512];if(bits==12){fat[0]=(byte)0xf8;fat[1]=(byte)0xff;fat[2]=(byte)0xff;}else{DiskTools.le16(fat,0,0xfff8);DiskTools.le16(fat,2,0xffff);}d.write(2049,fat,512);d.write(2049+l.fat,fat,512);byte[] root=new byte[512];DiskTools.text(root,0,label);root[11]=8;d.write(2049+2*l.fat,root,512);
        byte[] mbr=new byte[512];mbr[450]=(byte)(bits==12?1:0x0e);Arrays.fill(mbr,447,450,(byte)0xff);Arrays.fill(mbr,451,454,(byte)0xff);DiskTools.le32(mbr,454,2048);DiskTools.le32(mbr,458,l.volume);mbr[510]=0x55;mbr[511]=(byte)0xaa;p.update(end,end);d.write(0,mbr,512);d.sync();byte[] verify=new byte[512];d.read(2048,verify,512);if(!Arrays.equals(b,verify))throw new IOException("VERIFY_FAILED");
    }
}
