package org.usbworkbench;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.CRC32;

/** Original disk engines. Only explicit raw operations write; all parsers validate bounds. */
public final class AdvancedDiskTools {
    static long u32(byte[] b,int p){return (ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt(p) & 0xffffffffL);}
    static long u64(byte[] b,int p)throws IOException{long n=ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong(p);if(n<0)throw new IOException("DISK_BOUNDS");return n;}
    static void le64(byte[] b,int p,long n){ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).putLong(p,n);}
    static long ceil(long n,long d){return (n+d-1)/d;}
    static long align(long n,long a){return ceil(n,a)*a;}
    static long crc(byte[] b,int length){CRC32 c=new CRC32();c.update(b,0,length);return c.getValue();}
    public static final class ExFatLayout {
        public final long volume,fat,heap,clusters,bitmapBytes,bitmapClusters,upcaseClusters,root;
        public final int spc;
        public ExFatLayout(long total,int cluster)throws IOException{
            if(total<131072||total>0xffffffffL)throw new IOException("FAT_SIZE");volume=total-2048;
            spc=cluster==0?(total<524288?8:total<67108864?64:256):cluster;
            if(spc<1||spc>65536||(spc&(spc-1))!=0)throw new IOException("FAT_CLUSTER");
            fat=ceil((volume/spc+2)*4,512);heap=align(2048+fat,2048);clusters=(volume-heap)/spc;
            if(clusters<1||clusters>0xfffffff5L)throw new IOException("FAT_CLUSTER");
            bitmapBytes=ceil(clusters,8);bitmapClusters=ceil(bitmapBytes,spc*512L);upcaseClusters=ceil(upcase().length,spc*512L);root=2+bitmapClusters+upcaseClusters;
            if(root-1>clusters)throw new IOException("FAT_CLUSTER");
        }
    }
    public static void format(BlockDevice disk,String fs,String table,String label,int cluster,DiskTools.Progress p)throws IOException{
        if(disk.blockSize()!=512)throw new IOException("FAT_SECTOR");
        boolean gpt="gpt".equals(table);if(!gpt&&!"mbr".equals(table))throw new IOException("FORMAT_OPTIONS");
        long total=disk.blockCount()-(gpt?33:0);
        if("fat32".equals(fs)){new DiskTools.FatLayout(total,cluster);if(label.trim().isEmpty()||!label.matches("[A-Za-z0-9 _-]{1,11}"))throw new IOException("FAT_LABEL");}
        else if("fat16".equals(fs)||"fat12".equals(fs)){new Fat16.Layout(total,cluster,"fat12".equals(fs)?12:16);if(label.trim().isEmpty()||!label.matches("[A-Za-z0-9 _-]{1,11}"))throw new IOException("FAT_LABEL");}
        else if("exfat".equals(fs)){new ExFatLayout(total,cluster);checkExFatLabel(label);}else throw new IOException("FORMAT_OPTIONS");
        p.update(0,1);
        if(gpt){byte[] z=new byte[512];for(long s=disk.blockCount()-33;s<disk.blockCount();s++)disk.write(s,z,512);}
        BlockDevice volume=new BlockDevice(){
            public int blockSize(){return disk.blockSize();}public long blockCount(){return total;}
            public void read(long s,byte[] b,int n)throws IOException{disk.read(s,b,n);}public void write(long s,byte[] b,int n)throws IOException{disk.write(s,b,n);}public void sync()throws IOException{disk.sync();}public void close(){}
        };
        if("fat32".equals(fs))DiskTools.formatFat32(volume,label,cluster,p);else if("fat16".equals(fs)||"fat12".equals(fs))Fat16.format(volume,label,cluster,"fat12".equals(fs)?12:16,p);else formatExFat(volume,label,cluster,p);
        if(gpt){p.update(0,1);installGpt(disk,2048,total-1,label);disk.sync();inspectGpt(disk);p.update(1,1);}
    }
    public static void checkExFatLabel(String label)throws IOException{
        if(label.isEmpty()||label.trim().isEmpty()||label.length()>15)throw new IOException("EXFAT_LABEL");
        for(int i=0;i<label.length();i++){char c=label.charAt(i);if(c<32||"\"*/:<>?\\|".indexOf(c)>=0||Character.isSurrogate(c))throw new IOException("EXFAT_LABEL");}
    }
    public static byte[] upcase(){
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        for(int i=0;i<65536;){
            int run=0;while(i+run<65536 && Character.toUpperCase((char)(i+run))==(char)(i+run) && run<65535)run++;
            if(run>=2){out.write(255);out.write(255);out.write(run&255);out.write(run>>>8);i+=run;}
            else{int value=Character.toUpperCase((char)i);out.write(value&255);out.write(value>>>8);i++;}
        }return out.toByteArray();
    }
    public static long exfatChecksum(byte[] b,boolean boot){long sum=0;for(int i=0;i<b.length;i++){if(boot&&(i==106||i==107||i==112))continue;sum=((sum>>>1)|((sum&1)<<31))+(b[i]&255);sum&=0xffffffffL;}return sum;}
    public static void formatExFat(BlockDevice disk,String label,int cluster,DiskTools.Progress p)throws IOException{
        if(disk.blockSize()!=512)throw new IOException("FAT_SECTOR");checkExFatLabel(label);ExFatLayout l=new ExFatLayout(disk.blockCount(),cluster);
        byte[] up=upcase();long allocated=l.root-1,heapStart=2048+l.heap,rootLba=heapStart+(l.root-2)*l.spc,end=rootLba+l.spc;
        p.update(0,end);disk.write(0,new byte[512],512);byte[] z=new byte[16384];
        for(long s=1;s<end;){p.update(s,end);int n=(int)Math.min(32,end-s);disk.write(s,z,n*512);s+=n;}
        for(long s=disk.blockCount()-33;s<disk.blockCount();s++)disk.write(s,new byte[512],512);
        // One FAT; metadata files use explicit chains even though user files may use no-FAT-chain allocation.
        long[] starts={2,2+l.bitmapClusters,l.root},lengths={l.bitmapClusters,l.upcaseClusters,1};
        byte[] fat=new byte[512];long fatSector=0;DiskTools.le32(fat,0,0xfffffff8L);DiskTools.le32(fat,4,0xffffffffL);
        for(int file=0;file<starts.length;file++)for(long c=starts[file];c<starts[file]+lengths[file];c++){
            long sec=c/128;if(sec!=fatSector){disk.write(4096+fatSector,fat,512);fat=new byte[512];fatSector=sec;}DiskTools.le32(fat,(int)(c%128)*4,c+1<starts[file]+lengths[file]?c+1:0xffffffffL);
        }disk.write(4096+fatSector,fat,512);
        byte[] bitmap=new byte[16384];for(long offset=0;offset<l.bitmapBytes;){Arrays.fill(bitmap,(byte)0);int n=(int)Math.min(bitmap.length,l.bitmapBytes-offset);for(int i=0;i<n;i++){long bit=(offset+i)*8;int set=(int)Math.max(0,Math.min(8,allocated-bit));bitmap[i]=(byte)((1<<set)-1);}int padded=(n+511)/512*512;disk.write(heapStart+offset/512,bitmap,padded);offset+=n;}
        long upStart=heapStart+l.bitmapClusters*l.spc;for(int offset=0;offset<up.length;offset+=16384){int n=(int)align(Math.min(16384,up.length-offset),512);disk.write(upStart+offset/512,Arrays.copyOfRange(up,offset,offset+n),n);}
        byte[] root=new byte[512];root[0]=(byte)0x81;DiskTools.le32(root,20,2);le64(root,24,l.bitmapBytes);root[32]=(byte)0x82;DiskTools.le32(root,36,exfatChecksum(up,false));DiskTools.le32(root,52,2+l.bitmapClusters);le64(root,56,up.length);root[64]=(byte)0x83;root[65]=(byte)label.length();byte[] labelBytes=label.getBytes(StandardCharsets.UTF_16LE);System.arraycopy(labelBytes,0,root,66,labelBytes.length);disk.write(rootLba,root,512);
        byte[] region=new byte[11*512];region[0]=(byte)0xeb;region[1]=0x76;region[2]=(byte)0x90;DiskTools.text(region,3,"EXFAT   ");le64(region,64,2048);le64(region,72,l.volume);DiskTools.le32(region,80,2048);DiskTools.le32(region,84,l.fat);DiskTools.le32(region,88,l.heap);DiskTools.le32(region,92,l.clusters);DiskTools.le32(region,96,l.root);DiskTools.le32(region,100,System.currentTimeMillis());DiskTools.le16(region,104,0x100);region[108]=9;region[109]=(byte)Integer.numberOfTrailingZeros(l.spc);region[110]=1;region[111]=(byte)0x80;region[112]=(byte)(allocated*100/l.clusters);region[510]=0x55;region[511]=(byte)0xaa;
        for(int s=1;s<=8;s++)DiskTools.le32(region,s*512+508,0xaa550000L);
        long sum=exfatChecksum(region,true);byte[] checks=new byte[512];for(int i=0;i<512;i+=4)DiskTools.le32(checks,i,sum);
        disk.write(2048,region,region.length);disk.write(2060,region,region.length);disk.write(2059,checks,512);disk.write(2071,checks,512);
        byte[] mbr=new byte[512];mbr[447]=(byte)0xfe;mbr[448]=(byte)0xff;mbr[449]=(byte)0xff;mbr[450]=7;mbr[451]=(byte)0xfe;mbr[452]=(byte)0xff;mbr[453]=(byte)0xff;DiskTools.le32(mbr,454,2048);DiskTools.le32(mbr,458,l.volume);mbr[510]=0x55;mbr[511]=(byte)0xaa;
        p.update(end,end);disk.write(0,mbr,512);disk.sync();byte[] read=new byte[region.length];disk.read(2048,read,read.length);if(!Arrays.equals(read,region))throw new IOException("VERIFY_FAILED");disk.read(2060,read,read.length);if(!Arrays.equals(read,region))throw new IOException("VERIFY_FAILED");
    }
    static byte[] guid(UUID id){ByteBuffer b=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);long msb=id.getMostSignificantBits(),lsb=id.getLeastSignificantBits();b.putInt((int)(msb>>>32)).putShort((short)(msb>>>16)).putShort((short)msb);b.order(ByteOrder.BIG_ENDIAN).putLong(lsb);return b.array();}
    static byte[] gptHeader(long at,long backup,long entries,long last,byte[] diskId,long entriesCrc){byte[] h=new byte[512];DiskTools.text(h,0,"EFI PART");DiskTools.le32(h,8,0x10000);DiskTools.le32(h,12,92);le64(h,24,at);le64(h,32,backup);le64(h,40,34);le64(h,48,last-33);System.arraycopy(diskId,0,h,56,16);le64(h,72,entries);DiskTools.le32(h,80,128);DiskTools.le32(h,84,128);DiskTools.le32(h,88,entriesCrc);DiskTools.le32(h,16,crc(h,92));return h;}
    static void installGpt(BlockDevice d,long start,long end,String label)throws IOException{
        installGpt(d,start,end,label,false);
    }
    static void installGpt(BlockDevice d,long start,long end,String label,boolean linux)throws IOException{installGpt(d,start,end,label,linux?"0FC63DAF-8483-4772-8E79-3D69D8477DE4":"EBD0A0A2-B9E5-4433-87C0-68B6B72699C7");}
    static void installGpt(BlockDevice d,long start,long end,String label,String type)throws IOException{
        long last=d.blockCount()-1;byte[] entries=new byte[16384];System.arraycopy(guid(UUID.fromString(type)),0,entries,0,16);System.arraycopy(guid(UUID.randomUUID()),0,entries,16,16);le64(entries,32,start);le64(entries,40,end);byte[] name=label.getBytes(StandardCharsets.UTF_16LE);System.arraycopy(name,0,entries,56,Math.min(72,name.length));byte[] diskId=guid(UUID.randomUUID());long ec=crc(entries,entries.length);byte[] primary=gptHeader(1,last,2,last,diskId,ec),backup=gptHeader(last,1,last-32,last,diskId,ec);
        d.write(last-32,entries,entries.length);d.write(last,backup,512);d.write(2,entries,entries.length);d.write(1,primary,512);
        byte[] mbr=new byte[512];mbr[447]=0;mbr[448]=2;mbr[449]=0;mbr[450]=(byte)0xee;Arrays.fill(mbr,451,454,(byte)0xff);DiskTools.le32(mbr,454,1);DiskTools.le32(mbr,458,Math.min(0xffffffffL,last));mbr[510]=0x55;mbr[511]=(byte)0xaa;d.write(0,mbr,512);
    }
    public static String inspectGpt(BlockDevice d)throws IOException{
        if(d.blockSize()!=512&&d.blockSize()!=4096)throw new IOException("FAT_SECTOR");
        byte[] h=new byte[d.blockSize()];d.read(1,h,h.length);if(!new String(h,0,8,StandardCharsets.US_ASCII).equals("EFI PART"))return "GPT: none / لا يوجد\n";
        long headerSize=u32(h,12),expected=u32(h,16);if(headerSize<92||headerSize>h.length)throw new IOException("GPT_INVALID");DiskTools.le32(h,16,0);if(crc(h,(int)headerSize)!=expected)throw new IOException("GPT_CRC");
        long count=u32(h,80),size=u32(h,84),at=u64(h,72),first=u64(h,40),last=u64(h,48),backup=u64(h,32);if(u64(h,24)!=1||backup!=d.blockCount()-1||first>last||last>=backup||size<128||size%128!=0||count>4096||size>1024||count*size>1048576||count==0)throw new IOException("GPT_INVALID");
        int bytes=(int)(count*size),rounded=(int)(ceil(bytes,d.blockSize())*d.blockSize());if(at<2||at+rounded/d.blockSize()>first)throw new IOException("GPT_INVALID");byte[] entries=new byte[rounded];for(int off=0;off<rounded;off+=16384){int n=Math.min(16384,rounded-off);byte[] part=new byte[n];d.read(at+off/d.blockSize(),part,n);System.arraycopy(part,0,entries,off,n);}if(crc(entries,bytes)!=u32(h,88))throw new IOException("GPT_CRC");
        byte[] bh=new byte[d.blockSize()];d.read(backup,bh,bh.length);if(!new String(bh,0,8,StandardCharsets.US_ASCII).equals("EFI PART")||u64(bh,24)!=backup||u64(bh,32)!=1||u32(bh,12)!=headerSize)throw new IOException("GPT_INVALID");long bc=u32(bh,16);DiskTools.le32(bh,16,0);if(crc(bh,(int)headerSize)!=bc||u32(bh,88)!=u32(h,88))throw new IOException("GPT_CRC");
        long backupAt=u64(bh,72);
        if(u32(bh,80)!=count||u32(bh,84)!=size||backupAt<=last||backupAt+rounded/d.blockSize()>backup||u64(bh,40)!=first||u64(bh,48)!=last||!Arrays.equals(Arrays.copyOfRange(h,56,72),Arrays.copyOfRange(bh,56,72)))throw new IOException("GPT_INVALID");
        byte[] backupEntries=new byte[rounded];for(int off=0;off<rounded;off+=16384){int n=Math.min(16384,rounded-off);byte[] part=new byte[n];d.read(backupAt+off/d.blockSize(),part,n);System.arraycopy(part,0,backupEntries,off,n);}if(!Arrays.equals(entries,backupEntries))throw new IOException("GPT_CRC");
        StringBuilder text=new StringBuilder("GPT • CRC OK / سليم\n");for(int i=0;i<count;i++){int off=(int)(i*size);boolean empty=true;for(int j=0;j<16;j++)if(entries[off+j]!=0)empty=false;if(empty)continue;long start=u64(entries,off+32),end=u64(entries,off+40);if(start<first||end>last||start>end)throw new IOException("GPT_INVALID");String name=new String(entries,off+56,72,StandardCharsets.UTF_16LE).split("\u0000",2)[0];text.append(i+1).append(" • ").append(name).append(" • LBA ").append(start).append("–").append(end).append('\n');}return text.toString();
    }
    static void pattern(byte[] b,long lba,long seed,int sector){for(int offset=0;offset<b.length;offset+=sector){long x=(lba+offset/sector)^seed;for(int at=offset;at<offset+sector;at+=8){x+=0x9e3779b97f4a7c15L;long z=x;z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;z=(z^(z>>>27))*0x94d049bb133111ebL;z^=z>>>31;for(int j=0;j<8;j++)b[at+j]=(byte)(z>>>(8*j));}}}
    public static void capacityTest(BlockDevice d,DiskTools.Progress p)throws IOException{capacityTest(d,1,p);}
    public static void capacityTest(BlockDevice d,int passes,DiskTools.Progress p)throws IOException{
        if(passes!=1&&passes!=3)throw new IOException("CAPACITY_OPTIONS");long size=d.blockCount()*d.blockSize();if(size<=0||size>Long.MAX_VALUE/(2*passes))throw new IOException("DISK_BOUNDS");byte[] expected=new byte[16384],actual=new byte[16384];SecureRandom random=new SecureRandom();long previous=0;
        for(int pass=0;pass<passes;pass++){long seed;do{seed=random.nextLong();}while(pass>0&&seed==previous);previous=seed;long base=pass*size*2,total=size*2*passes;
            // Complete a whole-drive write before each read pass: wrapped fake media cannot pass.
            for(long done=0;done<size;){p.update(base+done,total);pattern(expected,done/d.blockSize(),seed,d.blockSize());int n=(int)Math.min(expected.length,size-done);d.write(done/d.blockSize(),expected,n);done+=n;}d.sync();
            for(long done=0;done<size;){p.update(base+size+done,total);pattern(expected,done/d.blockSize(),seed,d.blockSize());int n=(int)Math.min(expected.length,size-done);d.read(done/d.blockSize(),actual,n);for(int i=0;i<n;i++)if(expected[i]!=actual[i])throw new IOException("CAPACITY_MISMATCH @ "+(done+i)+" / pass="+(pass+1));done+=n;}
        }p.update(size*2*passes,size*2*passes);
    }
}
