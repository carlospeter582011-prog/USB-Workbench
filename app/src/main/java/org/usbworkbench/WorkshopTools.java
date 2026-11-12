package org.usbworkbench;
import java.io.*;
import java.util.*;
import java.security.*;
import java.util.zip.*;
/** Streaming file tools; split parts have checksummed headers and are not boot images. */
public final class WorkshopTools {
    public interface PartSink {OutputStream open(int index)throws IOException;}
    public interface Source {InputStream open()throws IOException;}
    public static void split(InputStream source,long size,int partSize,PartSink sink,DiskTools.Progress p)throws IOException{
        if(size<1||partSize<1048576||partSize>2146435072||size/partSize>99998)throw new IOException("SPLIT_SIZE");
        byte[] buffer=new byte[65536];long done=0;int index=0;
        try(InputStream in=source){while(done<size){int length=(int)Math.min(partSize,size-done);MessageDigest hash=sha();
            try(DataOutputStream out=new DataOutputStream(sink.open(index))){out.writeBytes("USBWBP01");out.writeLong(size);out.writeInt(index++);out.writeInt(length);
                int remaining=length;while(remaining>0){p.update(done,size);int n=in.read(buffer,0,Math.min(buffer.length,remaining));if(n<0)throw new IOException("SOURCE_SHORT");if(n==0)throw new IOException("SOURCE_STALLED");out.write(buffer,0,n);hash.update(buffer,0,n);remaining-=n;done+=n;}out.write(hash.digest());}
        }if(in.read()!=-1)throw new IOException("SOURCE_CHANGED");}p.update(done,size);
    }
    public static final class Part {public final Source source;public final long size;public final int index,length;
        public Part(Source source)throws IOException{this.source=source;try(DataInputStream in=new DataInputStream(source.open())){byte[] magic=new byte[8];in.readFully(magic);if(!Arrays.equals(magic,"USBWBP01".getBytes(java.nio.charset.StandardCharsets.US_ASCII)))throw new IOException("PART_INVALID");size=in.readLong();index=in.readInt();length=in.readInt();if(size<1||index<0||index>99998||length<1||length>2146435072)throw new IOException("PART_INVALID");}}
    }
    public static void join(List<Part> supplied,OutputStream destination,DiskTools.Progress p)throws IOException{
        List<Part> parts=new ArrayList<>(supplied);Collections.sort(parts,(a,b)->Integer.compare(a.index,b.index));if(parts.isEmpty())throw new IOException("PART_MISSING");long size=parts.get(0).size,sum=0;
        for(int i=0;i<parts.size();i++){Part part=parts.get(i);if(part.index!=i||part.size!=size||part.length>size-sum)throw new IOException("PART_MISSING");sum+=part.length;}if(sum!=size)throw new IOException("PART_MISSING");
        byte[] buffer=new byte[65536];long done=0;
        for(Part part:parts)try(DataInputStream in=new DataInputStream(part.source.open())){byte[] header=new byte[24];in.readFully(header);DataInputStream h=new DataInputStream(new ByteArrayInputStream(header));byte[] magic=new byte[8];h.readFully(magic);if(!Arrays.equals(magic,"USBWBP01".getBytes(java.nio.charset.StandardCharsets.US_ASCII))||h.readLong()!=part.size||h.readInt()!=part.index||h.readInt()!=part.length)throw new IOException("SOURCE_CHANGED");MessageDigest hash=sha();int remaining=part.length;
            while(remaining>0){p.update(done,size);int n=in.read(buffer,0,Math.min(buffer.length,remaining));if(n<0)throw new IOException("PART_INVALID");if(n==0)throw new IOException("SOURCE_STALLED");destination.write(buffer,0,n);hash.update(buffer,0,n);remaining-=n;done+=n;}byte[] digest=new byte[32];in.readFully(digest);if(!MessageDigest.isEqual(digest,hash.digest())||in.read()!=-1)throw new IOException("PART_CORRUPT");
        }destination.flush();p.update(done,size);
    }
    private static MessageDigest sha(){try{return MessageDigest.getInstance("SHA-256");}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
    public static String checksums(InputStream source,long size,String expected,DiskTools.Progress p)throws IOException{
        if(!expected.isEmpty()&&!expected.matches("[a-fA-F0-9]{64}"))throw new IOException("HASH_EXPECTED");String[] names={"MD5","SHA-1","SHA-256","SHA-512"};MessageDigest[] hashes=new MessageDigest[4];try{for(int i=0;i<4;i++)hashes[i]=MessageDigest.getInstance(names[i]);}catch(NoSuchAlgorithmException e){throw new IOException("HASH_UNAVAILABLE",e);}
        long done=0;byte[] b=new byte[65536];try(InputStream in=source){int n;while((n=in.read(b))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");p.update(done,size);for(MessageDigest hash:hashes)hash.update(b,0,n);done+=n;}}if(size>=0&&done!=size)throw new IOException("SOURCE_CHANGED");p.update(done,size);StringBuilder out=new StringBuilder("Checksums / بصمات التحقق\n");
        for(int i=0;i<4;i++){StringBuilder hex=new StringBuilder();for(byte value:hashes[i].digest())hex.append(String.format(Locale.ROOT,"%02x",value&255));out.append(names[i]).append(": ").append(hex).append('\n');if(i==2&&!expected.isEmpty())out.append(hex.toString().equalsIgnoreCase(expected)?"SHA-256 MATCH / تطابق\n":"SHA-256 MISMATCH / عدم تطابق\n");}
        return out.append("MD5/SHA-1: legacy comparison only, not authenticity proof.\nMD5/SHA-1 للمقارنة القديمة فقط، وليستا إثبات أصالة.").toString();
    }
    public static String inspectImage(InputStream source,long size,DiskTools.Progress p)throws IOException{
        byte[] bytes=new byte[65536];int count=0;try(InputStream in=source){while(count<bytes.length){int n=in.read(bytes,count,bytes.length-count);if(n<0)break;if(n==0)throw new IOException("SOURCE_STALLED");count+=n;p.update(count,Math.min(bytes.length,size));}}
        StringBuilder text=new StringBuilder("Image signatures / تواقيع الصورة\n");text.append("Bytes / البايتات: ").append(size).append('\n');
        if(count>=512&&bytes[510]==0x55&&(bytes[511]&255)==0xaa){text.append("MBR signature / توقيع MBR\n");for(int i=0;i<4;i++){int at=446+i*16;int type=bytes[at+4]&255;if(type!=0)text.append(i+1).append(" • type ").append(Integer.toHexString(type)).append(" • start ").append(AdvancedDiskTools.u32(bytes,at+8)).append(" • sectors ").append(AdvancedDiskTools.u32(bytes,at+12)).append('\n');}}
        if(count>=520&&new String(bytes,512,8,java.nio.charset.StandardCharsets.US_ASCII).equals("EFI PART"))text.append("GPT header present / توجد ترويسة GPT\n");
        if(count>=32775&&new String(bytes,32769,5,java.nio.charset.StandardCharsets.US_ASCII).equals("CD001"))text.append("ISO9660 descriptor / واصف ISO9660\n");
        text.append("Signatures only; does not prove bootability or filesystem integrity.\nتواقيع فقط؛ لا تثبت قابلية الإقلاع أو سلامة نظام الملفات.");return text.toString();
    }
    public static String randomRead(BlockDevice d,DiskTools.Progress p)throws IOException{
        int n=65536;long sectors=n/d.blockSize(),slots=d.blockCount()/sectors;if(slots<1)throw new IOException("DISK_BOUNDS");byte[] b=new byte[n];Random random=new Random(194775);long start=System.nanoTime();
        for(int i=0;i<512;i++){p.update(i,512);long slot=(random.nextLong()&Long.MAX_VALUE)%slots;d.read(slot*sectors,b,n);}p.update(512,512);double sec=(System.nanoTime()-start)/1e9;return String.format(Locale.ROOT,"Random read / قراءة عشوائية: %.2f MiB/s\nAverage request / متوسط الطلب: %.2f ms\n512 × 64 KiB; no writes / دون كتابة",32/sec,sec*1000/512);
    }
    public static String scan(BlockDevice d,DiskTools.Progress p)throws IOException{byte[] b=new byte[16384];long capacity=d.blockCount()*d.blockSize();for(long done=0;done<capacity;){p.update(done,capacity);int n=(int)Math.min(b.length,capacity-done);d.read(done/d.blockSize(),b,n);done+=n;}p.update(capacity,capacity);return "All sectors readable / أمكن قراءة كل القطاعات\nNot a fake-capacity or lifetime test / ليس اختبار سعة مزيفة أو ضمان عمر الوحدة";}
    public static String hex(BlockDevice d)throws IOException{byte[] b=new byte[d.blockSize()];d.read(0,b,b.length);StringBuilder out=new StringBuilder("First sector • read only / القطاع الأول • قراءة فقط\n");for(int at=0;at<b.length;at+=16){out.append(String.format(Locale.ROOT,"%04X  ",at));for(int i=at;i<at+16;i++)out.append(String.format(Locale.ROOT,"%02X ",b[i]&255));out.append(' ');for(int i=at;i<at+16;i++)out.append(b[i]>=32&&b[i]<=126?(char)b[i]:'.');out.append('\n');}return out.toString();}
}
