package org.usbworkbench;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
public class AdvancedToolsTest {
    static int checks=0;static void check(boolean c,String m){checks++;if(!c)throw new AssertionError(m);}
    interface Action{void run()throws Exception;}
    static void fails(String code,Action a)throws Exception{try{a.run();throw new AssertionError("Expected "+code);}catch(IOException e){check(e.getMessage().startsWith(code),e.toString());}}
    static DiskTools.Progress p=(a,b)->{};
    static long u32(byte[] b,int at){return AdvancedDiskTools.u32(b,at);}
    static void format()throws Exception{
        for(String fs:new String[]{"fat32","exfat"})for(String table:new String[]{"mbr","gpt"}){
            DiskToolsTest.MemoryDisk d=new DiskToolsTest.MemoryDisk(262144,512);AdvancedDiskTools.format(d,fs,table,fs.equals("exfat")?"وحدة USB":"TEST_USB",0,p);
            byte[] m=d.at(0);check(m[510]==0x55&&(m[511]&255)==0xaa,"MBR signature "+fs+table);
            if(table.equals("gpt")){String text=AdvancedDiskTools.inspectGpt(d);check(text.contains("CRC OK")&&text.contains("2048"),"GPT parsed "+fs);byte[] header=d.at(1);long finalLba=AdvancedDiskTools.u64(header,48);check(finalLba==d.count-34,"backup table reserved");byte[] corrupt=d.at(2);corrupt[56]^=1;d.write(2,corrupt,512);fails("GPT_CRC",()->AdvancedDiskTools.inspectGpt(d));d.write(2,new byte[512],512);}
            if(fs.equals("exfat")){
                byte[] boot=d.at(2048);check(new String(boot,3,8,StandardCharsets.US_ASCII).equals("EXFAT   "),"exfat signature");check(boot[108]==9&&boot[110]==1,"sector/fat geometry");
                long heap=u32(boot,88),fat=u32(boot,84),count=u32(boot,92),root=u32(boot,96);int spc=1<<(boot[109]&255);
                check(count>0&&fat*128>=count+2,"FAT fits heap");check((heap+count*spc)<=AdvancedDiskTools.u64(boot,72),"heap within volume");
                byte[] region=new byte[11*512];d.read(2048,region,region.length);long sum=AdvancedDiskTools.exfatChecksum(region,true);byte[] cs=d.at(2059);for(int i=0;i<512;i+=4)check(u32(cs,i)==sum,"boot checksum copies");
                byte[] backup=new byte[region.length];d.read(2060,backup,backup.length);check(Arrays.equals(region,backup),"exfat backup boot");byte[] directory=d.at(2048+heap+(root-2)*spc);check((directory[0]&255)==0x81&&(directory[32]&255)==0x82&&(directory[64]&255)==0x83,"bitmap/upcase/label directory entries");
                long bm=u32(directory,20);byte[] bitmap=d.at(2048+heap+(bm-2)*spc);for(int i=0;i<root-1;i++)check((bitmap[i/8]&(1<<(i%8)))!=0,"metadata cluster allocated");check((bitmap[(int)(root-1)/8]&(1<<((root-1)%8)))==0,"first free cluster unallocated");
                byte[] up=AdvancedDiskTools.upcase();check(up.length<65536&&u32(directory,36)==AdvancedDiskTools.exfatChecksum(up,false),"full upcase and checksum");int[] mapping=new int[65536];int code=0;for(int k=0;k<up.length;k+=2){int v=(up[k]&255)|((up[k+1]&255)<<8);if(v==65535 && k+2<up.length){k+=2;int run=(up[k]&255)|((up[k+1]&255)<<8);for(int j=0;j<run;j++)mapping[code]=code++;}else mapping[code++]=v;}check(code==65536&&mapping['a']=='A'&&mapping[65535]==65535,"compressed table covers BMP and mandatory ASCII");
            }
        }
        DiskToolsTest.MemoryDisk untouched=new DiskToolsTest.MemoryDisk(262144,512);fails("EXFAT_LABEL",()->AdvancedDiskTools.format(untouched,"exfat","gpt","bad/name",0,p));fails("FORMAT_OPTIONS",()->AdvancedDiskTools.format(untouched,"ntfs","mbr","USB",0,p));check(untouched.writes==0,"invalid format writes nothing");
        DiskToolsTest.MemoryDisk exported=new DiskToolsTest.MemoryDisk(262144,512);AdvancedDiskTools.format(exported,"exfat","mbr","USBTEST",0,p);
        try(RandomAccessFile out=new RandomAccessFile("artifacts/exfat-test-volume.img","rw")){out.setLength(0);out.setLength((exported.count-2048)*512);for(Map.Entry<Long,byte[]> e:exported.sectors.entrySet())if(e.getKey()>=2048){out.seek((e.getKey()-2048)*512);out.write(e.getValue());}}
        DiskToolsTest.MemoryDisk fatDisk=new DiskToolsTest.MemoryDisk(262144,512);AdvancedDiskTools.format(fatDisk,"fat32","mbr","USBTEST",0,p);
        try(RandomAccessFile out=new RandomAccessFile("artifacts/fat32-test-volume.img","rw")){out.setLength(0);out.setLength((fatDisk.count-2048)*512);for(Map.Entry<Long,byte[]> e:fatDisk.sectors.entrySet())if(e.getKey()>=2048){out.seek((e.getKey()-2048)*512);out.write(e.getValue());}}
    }
    static void capacity()throws Exception{
        DiskToolsTest.MemoryDisk real=new DiskToolsTest.MemoryDisk(73,512);AdvancedDiskTools.capacityTest(real,p);check(real.sectors.size()==73,"capacity test whole disk");
        BlockDevice fake=new DiskToolsTest.MemoryDisk(128,512){@Override public void write(long lba,byte[] b,int n)throws IOException{for(int i=0;i<n/512;i++)super.write((lba+i)%64,Arrays.copyOfRange(b,i*512,(i+1)*512),512);}@Override public void read(long lba,byte[] b,int n)throws IOException{for(int i=0;i<n/512;i++){byte[] part=new byte[512];super.read((lba+i)%64,part,512);System.arraycopy(part,0,b,i*512,512);}}};
        fails("CAPACITY_MISMATCH",()->AdvancedDiskTools.capacityTest(fake,p));
        DiskToolsTest.MemoryDisk empty=new DiskToolsTest.MemoryDisk(100,512);fails("CANCELLED",()->AdvancedDiskTools.capacityTest(empty,(a,b)->{throw new IOException("CANCELLED");}));check(empty.writes==0,"cancel before capacity write");
    }
    static byte[] zip(String[] names,byte[][] bytes)throws IOException{ByteArrayOutputStream data=new ByteArrayOutputStream();try(ZipOutputStream out=new ZipOutputStream(data)){for(int i=0;i<names.length;i++){out.putNextEntry(new ZipEntry(names[i]));out.write(bytes[i]);out.closeEntry();}}return data.toByteArray();}
    static void archives()throws Exception{
        Map<String,ByteArrayOutputStream> files=new HashMap<>();Set<String> dirs=new HashSet<>();ZipTools.Sink sink=new ZipTools.Sink(){public void directory(String path){dirs.add(path);}public OutputStream file(String path){ByteArrayOutputStream o=new ByteArrayOutputStream();files.put(path,o);return o;}};
        byte[] z=zip(new String[]{"folder/","folder/hello.txt","عربي.txt"},new byte[][]{new byte[0],"hello".getBytes(),new byte[]{1,2,3}});check(ZipTools.extract(new ByteArrayInputStream(z),sink,100,10,p)==8,"ZIP expanded count");check(files.get("folder/hello.txt").toString().equals("hello")&&dirs.contains("folder"),"ZIP contents");
        for(String name:new String[]{"../evil","/absolute","C:/drive","a\\b","a//b","./a","a/../../b","a/\u0000b"})fails("ZIP_PATH",()->ZipTools.safePath(name));
        fails("ZIP_LIMIT",()->ZipTools.extract(new ByteArrayInputStream(z),sink,2,10,p));fails("ZIP_LIMIT",()->ZipTools.extract(new ByteArrayInputStream(z),sink,100,1,p));
        byte[] duplicate=zip(new String[]{"a","A"},new byte[][]{new byte[0],new byte[0]});fails("ZIP_DUPLICATE",()->ZipTools.extract(new ByteArrayInputStream(duplicate),sink,100,10,p));
        byte[] badDirectory=zip(new String[]{"bad/"},new byte[][]{new byte[]{1,2,3}});fails("ZIP_PATH",()->ZipTools.extract(new ByteArrayInputStream(badDirectory),sink,100,10,p));
        byte[] traversal=zip(new String[]{"../outside"},new byte[][]{new byte[]{7}});fails("ZIP_PATH",()->ZipTools.extract(new ByteArrayInputStream(traversal),sink,100,10,p));
        fails("CANCELLED",()->ZipTools.extract(new ByteArrayInputStream(z),sink,100,10,(a,b)->{throw new IOException("CANCELLED");}));
    }
    public static void main(String[] args)throws Exception{format();capacity();archives();System.out.println("PASS: "+checks+" advanced exFAT/GPT, fake-capacity and ZIP safety checks");}
}
