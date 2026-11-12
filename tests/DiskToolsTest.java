package org.usbworkbench;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** No Android runtime, network, external test framework or physical disk required. */
public class DiskToolsTest {
    private static int checks=0;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    interface Action{void run()throws Exception;}
    static void fails(String code,Action action)throws Exception{try{action.run();throw new AssertionError("Expected "+code);}catch(IOException e){check(e.getMessage().startsWith(code),"Wrong error "+e);}}
    static class MemoryDisk implements BlockDevice {
        final int size;final long count;final Map<Long,byte[]> sectors=new HashMap<>();int writes=0,syncs=0;
        MemoryDisk(long count,int size){this.count=count;this.size=size;}
        public int blockSize(){return size;}public long blockCount(){return count;}
        private void bounds(long lba,int n)throws IOException{if(lba<0||n<=0||n%size!=0||lba+n/size>count)throw new IOException("DISK_BOUNDS");}
        public void read(long lba,byte[] bytes,int n)throws IOException{bounds(lba,n);for(int i=0;i<n/size;i++){byte[] b=sectors.get(lba+i);if(b==null)Arrays.fill(bytes,i*size,(i+1)*size,(byte)0);else System.arraycopy(b,0,bytes,i*size,size);}}
        public void write(long lba,byte[] bytes,int n)throws IOException{bounds(lba,n);writes++;for(int i=0;i<n/size;i++){byte[] b=Arrays.copyOfRange(bytes,i*size,(i+1)*size);boolean zero=true;for(byte v:b)if(v!=0){zero=false;break;}if(zero)sectors.remove(lba+i);else sectors.put(lba+i,b);}}
        public void sync(){syncs++;}public void close(){}
        byte[] at(long lba)throws IOException{byte[] b=new byte[size];read(lba,b,size);return b;}
    }
    static long u32(byte[] b,int p){return (b[p]&255L)|((b[p+1]&255L)<<8)|((b[p+2]&255L)<<16)|((b[p+3]&255L)<<24);}
    static int u16(byte[] b,int p){return (b[p]&255)|((b[p+1]&255)<<8);}
    static DiskTools.Progress progress=(a,b)->{if(a<0)throw new AssertionError("negative progress");};
    static void formatTests()throws Exception{
        MemoryDisk d=new MemoryDisk(1024*1024,512);
        byte[] stale=new byte[512];Arrays.fill(stale,(byte)0x7e);d.write(1,stale,512);d.write(d.count-1,stale,512);
        DiskTools.formatFat32(d,"MY_USB",0,progress);
        byte[] mbr=d.at(0),boot=d.at(2048),info=d.at(2049);
        check(mbr[510]==0x55&&(mbr[511]&255)==0xaa,"MBR signature");
        check((mbr[450]&255)==12,"FAT32 LBA partition type");
        check(u32(mbr,454)==2048,"1MiB alignment");check(u32(mbr,458)==d.count-2048,"partition covers remaining disk");
        check(u16(boot,11)==512&&boot[16]==2&&u16(boot,14)==32,"BPB geometry");
        check(u32(boot,32)==d.count-2048&&u32(boot,44)==2,"BPB total and root");
        check(Arrays.equals(boot,d.at(2054)),"backup boot equals primary");check(Arrays.equals(info,d.at(2055)),"backup FSInfo equals primary");
        check(u32(info,0)==0x41615252L&&u32(info,484)==0x61417272L&&u32(info,508)==0xaa550000L,"FSInfo signatures");
        long fatSectors=u32(boot,36),clusters=(u32(boot,32)-32-2*fatSectors)/(boot[13]&255);
        check(clusters>=65525&&fatSectors*512/4>=clusters+2,"valid FAT32 count and FAT capacity");
        check(u32(info,488)==clusters-1&&u32(info,492)==3,"free cluster hint excludes root");
        byte[] fat=d.at(2080);check(u32(fat,0)==0x0ffffff8L&&u32(fat,4)==0xffffffffL&&u32(fat,8)==0x0fffffffL,"reserved FAT entries and root EOC");
        check(Arrays.equals(fat,d.at(2080+fatSectors)),"mirrored FAT");
        byte[] root=d.at(2080+2*fatSectors);check(new String(root,0,11,StandardCharsets.US_ASCII).equals("MY_USB     ")&&root[11]==8,"volume label directory entry");
        check(u32(d.at(1),0)==0&&u32(d.at(d.count-1),0)==0,"old GPT headers cleared");check(d.syncs==1,"format flushes writes");
        for(long count:new long[]{131072,1048576,16777216,67108864,0xffffffffL}){
            DiskTools.FatLayout l=new DiskTools.FatLayout(count,0);check(l.clusters>=65525,"auto cluster valid "+count);check(l.fatSectors*128>=l.clusters+2,"FAT fits "+count);
        }
        MemoryDisk empty=new MemoryDisk(1024*1024,512);
        fails("FAT_LABEL",()->DiskTools.formatFat32(empty,"           ",0,progress));
        fails("FAT_LABEL",()->DiskTools.formatFat32(empty,"bad/name",0,progress));
        fails("FAT_CLUSTER",()->DiskTools.formatFat32(empty,"USB",3,progress));
        fails("FAT_SIZE",()->DiskTools.formatFat32(new MemoryDisk(1000,512),"USB",0,progress));
        fails("FAT_SECTOR",()->DiskTools.formatFat32(new MemoryDisk(1000000,4096),"USB",0,progress));check(empty.writes==0,"invalid format never writes");
        MemoryDisk cancelled=new MemoryDisk(1024*1024,512);
        fails("CANCELLED",()->DiskTools.formatFat32(cancelled,"USB",0,(a,b)->{throw new IOException("CANCELLED");}));
        check(u16(cancelled.at(0),510)==0,"cancelled format has no completed MBR");
    }
    static void imageTests()throws Exception{
        Random r=new Random(7);
        for(int sector:new int[]{512,4096})for(int size:new int[]{1,511,512,4096,16384,17003,32768}){
            MemoryDisk d=new MemoryDisk(128,sector);byte[] image=new byte[size];r.nextBytes(image);
            DiskTools.writeImage(d,new ByteArrayInputStream(image),size,progress);DiskTools.verifyImage(d,new ByteArrayInputStream(image),size,progress);
            int rounded=(size+sector-1)/sector*sector;ByteArrayOutputStream backup=new ByteArrayOutputStream();DiskTools.backup(d,backup,progress);byte[] all=backup.toByteArray();
            check(Arrays.equals(image,Arrays.copyOf(all,size)),"unaligned image roundtrip "+sector+"/"+size);
            boolean padding=true;for(int i=size;i<rounded;i++)if(all[i]!=0)padding=false;check(padding,"zero padding");
            check(all.length==128*sector,"full capacity backup including unused space");
            image[0]^=1;fails("VERIFY_FAILED",()->DiskTools.verifyImage(d,new ByteArrayInputStream(image),size,progress));
            DiskTools.wipe(d,progress);check(d.sectors.isEmpty(),"zero-fill covers full disk");
        }
        MemoryDisk d=new MemoryDisk(1,512);
        fails("IMAGE_SIZE",()->DiskTools.writeImage(d,new ByteArrayInputStream(new byte[513]),513,progress));
        fails("IMAGE_SIZE",()->DiskTools.writeImage(d,new ByteArrayInputStream(new byte[1]),-1,progress));check(d.writes==0,"oversized/unknown input never writes");
        fails("SOURCE_SHORT",()->DiskTools.writeImage(d,new ByteArrayInputStream(new byte[1]),512,progress));check(d.writes==0,"short first chunk never writes");
        fails("CANCELLED",()->DiskTools.writeImage(d,new ByteArrayInputStream(new byte[512]),512,(a,b)->{throw new IOException("CANCELLED");}));check(d.writes==0,"early cancel never writes");
        check(DiskTools.sha256(new ByteArrayInputStream("abc".getBytes(StandardCharsets.US_ASCII)),3,progress).equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),"known SHA-256 vector");
    }
    public static void main(String[] args)throws Exception{formatTests();imageTests();System.out.println("PASS: "+checks+" disk safety, layout, image round-trip and checksum checks");}
}
