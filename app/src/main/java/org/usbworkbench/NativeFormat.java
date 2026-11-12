package org.usbworkbench;
import android.content.Context;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
/** Native metadata writes stream to a bounded partition, with tables committed last. */
public final class NativeFormat {
    public interface Stages {void stage(String name);}
    public static boolean supports(String fs){return ExtraFormat.supports(fs)||Arrays.asList("ntfs","ext2","ext3","ext4","f2fs").contains(fs);}
    public static void format(Context c,BlockDevice disk,String fs,String table,String label,int cluster,DiskTools.Progress p,NativeTools.Cancelled cancel)throws IOException{
        format(c,disk,fs,table,label,cluster,p,cancel,name->{});
    }
    public static void format(Context c,BlockDevice disk,String fs,String table,String label,int cluster,DiskTools.Progress p,NativeTools.Cancelled cancel,Stages stages)throws IOException{
        if(ExtraFormat.supports(fs)){ExtraFormat.format(c,disk,fs,table,label,cluster,p,cancel,stages);return;}
        if(!supports(fs)||(!"mbr".equals(table)&&!"gpt".equals(table)))throw new IOException("FORMAT_OPTIONS");
        if(disk.blockSize()!=512)throw new IOException("FAT_SECTOR");
        if(disk.blockCount()>0xffffffffL||disk.blockCount()<67584)throw new IOException("NATIVE_SIZE");
        boolean f2fs="f2fs".equals(fs),linux=!"ntfs".equals(fs),gpt="gpt".equals(table);String tool=f2fs?"mkfs_f2fs":linux?"mke2fs":"mkntfs",checker=f2fs?"fsck_f2fs":linux?"e2fsck":"ntfsinfo";
        if(f2fs&&disk.blockCount()<524288)throw new IOException("F2FS_SIZE");
        if(!NativeTools.available(c,tool)||!NativeTools.available(c,checker))throw new IOException("ENGINE_MISSING");
        if(label.trim().isEmpty()||label.indexOf('\0')>=0)throw new IOException(f2fs?"F2FS_LABEL":linux?"EXT_LABEL":"NTFS_LABEL");
        for(int i=0;i<label.length();i++)if(Character.isISOControl(label.charAt(i))||Character.isSurrogate(label.charAt(i)))throw new IOException(f2fs?"F2FS_LABEL":linux?"EXT_LABEL":"NTFS_LABEL");
        if(f2fs){if(label.length()>32||label.getBytes(StandardCharsets.UTF_8).length>128)throw new IOException("F2FS_LABEL");if(cluster!=0)throw new IOException("F2FS_CLUSTER");}
        else if(linux){if(label.getBytes(StandardCharsets.UTF_8).length>16)throw new IOException("EXT_LABEL");if(cluster!=0)throw new IOException("EXT_CLUSTER");}
        else {if(label.length()>32)throw new IOException("NTFS_LABEL");if(cluster!=0&&(cluster<1||cluster>128||(cluster&(cluster-1))!=0))throw new IOException("FAT_CLUSTER");}
        stages.stage("engine_probe");NativeTools.preflight(c,tool,p,cancel);NativeTools.preflight(c,checker,p,cancel);
        long count=disk.blockCount()-2048-(gpt?33:0);SliceDevice volume=new SliceDevice(disk,2048,count);
        stages.stage("partition_prepare");p.update(0,1);byte[] zero=new byte[16384];disk.write(0,zero,512);
        for(long at=1;at<2048;){p.update(at,2048);int n=(int)Math.min(32,2048-at);disk.write(at,zero,n*512);at+=n;}
        for(long at=disk.blockCount()-33;at<disk.blockCount();){p.update(at-(disk.blockCount()-33),33);int n=(int)Math.min(32,disk.blockCount()-at);disk.write(at,zero,n*512);at+=n;}
        stages.stage(f2fs?"f2fs_metadata":linux?"ext_metadata":"ntfs_metadata");NativeTools.format(c,volume,fs,label,cluster,p,cancel);
        stages.stage("filesystem_verify");
        byte[] proof=new byte[2048];volume.read(0,proof,proof.length);
        if(f2fs){F2fsInspector.inspect(volume);NativeTools.run(c,"fsck_f2fs",Arrays.asList("--dry-run","--no-kernel-check","/dev/usbworkbench"),volume,false,p,cancel);}
        else if(linux){if((proof[1080]&255)!=0x53||(proof[1081]&255)!=0xef)throw new IOException("VERIFY_FAILED");NativeTools.run(c,"e2fsck",Arrays.asList("-f","-n","/dev/usbworkbench"),volume,false,p,cancel);}
        else {NtfsInspector.inspect(volume);NativeTools.run(c,"ntfsinfo",Arrays.asList("-f","-m","/dev/usbworkbench"),volume,false,p,cancel);}
        stages.stage("partition_commit");p.update(0,1);
        if(gpt)AdvancedDiskTools.installGpt(disk,2048,2048+count-1,label,linux);
        else{byte[] mbr=new byte[512];mbr[450]=(byte)(linux?0x83:7);Arrays.fill(mbr,447,450,(byte)0xff);Arrays.fill(mbr,451,454,(byte)0xff);DiskTools.le32(mbr,454,2048);DiskTools.le32(mbr,458,count);mbr[510]=0x55;mbr[511]=(byte)0xaa;disk.write(0,mbr,512);byte[] check=new byte[512];disk.read(0,check,512);if(!Arrays.equals(mbr,check))throw new IOException("VERIFY_FAILED");}
        disk.sync();if(gpt)AdvancedDiskTools.inspectGpt(disk);p.update(1,1);
    }
}
