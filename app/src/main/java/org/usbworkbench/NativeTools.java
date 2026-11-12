package org.usbworkbench;
import android.content.Context;
import android.os.Build;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Only fixed bundled executables; no shell, root, downloaded runtime code or external executable path. */
public final class NativeTools {
    private static final Set<String> TOOLS=new HashSet<>(Arrays.asList("mkntfs","ntfsfix","ntfsls","ntfscat","ntfscp","ntfsinfo","ntfsclone","mke2fs","e2fsck","mkfs_f2fs","fsck_f2fs","fsck_fat","fsck_exfat","mkfs_udf","udfinfo","mkapfs","apfsck","mkfs_hfsplus","fsck_hfsplus","mkfs_btrfs","btrfs","mkfs_xfs","xfs_repair"));
    public static boolean available(Context context,String tool){return TOOLS.contains(tool)&&new File(context.getApplicationInfo().nativeLibraryDir,"lib"+tool+".so").isFile()&&new File(context.getApplicationInfo().nativeLibraryDir,"libusbwb_bridge.so").isFile();}
    public interface Cancelled{boolean get();}
    public static String run(Context context,String tool,List<String> args,BlockDevice disk,boolean write,DiskTools.Progress progress,Cancelled cancelled)throws IOException{
        return run(context,tool,args,disk,write,progress,cancelled,0);
    }
    public static String run(Context context,String tool,List<String> args,BlockDevice disk,boolean write,DiskTools.Progress progress,Cancelled cancelled,int acceptedStatus)throws IOException{
        if(!available(context,tool))throw new IOException("ENGINE_MISSING");
        File dir=new File(context.getApplicationInfo().nativeLibraryDir);List<String> command=new ArrayList<>();command.add(new File(dir,"lib"+tool+".so").getAbsolutePath());command.addAll(args);
        ProcessBuilder builder=new ProcessBuilder(command);Map<String,String> env=builder.environment();env.remove("LD_LIBRARY_PATH");env.put("LD_PRELOAD",new File(dir,"libusbwb_bridge.so").getAbsolutePath());env.put("USBWB_BYTES",Long.toString(disk.blockCount()*disk.blockSize()));env.put("USBWB_WRITE",write?"1":"0");env.put("LC_ALL","C.UTF-8");env.put("LANG","C.UTF-8");
        File config=new File(context.getFilesDir(),"mke2fs-usb.conf");
        if(!config.exists())try(OutputStream out=new FileOutputStream(config)){out.write(("[defaults]\nbase_features = sparse_super,large_file,filetype,resize_inode,dir_index,ext_attr\nblocksize = 4096\ninode_size = 256\ninode_ratio = 16384\n[fs_types]\next3 = {\nfeatures = has_journal\n}\next4 = {\nfeatures = has_journal,extent,huge_file,flex_bg,metadata_csum,64bit\n}\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));}
        env.put("MKE2FS_CONFIG",config.getAbsolutePath());
        Process child=builder.start();AtomicBoolean finished=new AtomicBoolean(false),timedOut=new AtomicBoolean(false);ByteArrayOutputStream log=new ByteArrayOutputStream();
        Thread stderr=new Thread(()->{try(InputStream in=child.getErrorStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){synchronized(log){int keep=Math.min(n,65536-log.size());if(keep>0)log.write(b,0,keep);}}}catch(IOException ignored){}},"native-log");stderr.setDaemon(true);stderr.start();
        Thread watch=new Thread(()->{long began=System.nanoTime();while(!finished.get()){if(cancelled.get()||System.nanoTime()-began>30L*60*1000000000){timedOut.set(!cancelled.get());child.destroy();if(Build.VERSION.SDK_INT>=26)child.destroyForcibly();try{child.getInputStream().close();}catch(IOException ignored){}return;}try{Thread.sleep(100);}catch(InterruptedException e){return;}}},"native-watch");watch.setDaemon(true);watch.start();
        try{
            NativeProtocol.serve(child.getInputStream(),child.getOutputStream(),disk,write,progress);
            int code;try{code=child.waitFor();stderr.join(2000);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("CANCELLED");}
            if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");
            String text; synchronized(log){text=new String(log.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);}
            if(code!=0&&code!=acceptedStatus)throw new IOException("ENGINE_FAILED "+tool+" ("+code+")\n"+text);
            return text;
        }catch(IOException e){if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");child.destroy();try{stderr.join(500);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();}synchronized(log){if(log.size()>0)e.addSuppressed(new IOException("ENGINE_LOG "+tool+"\n"+new String(log.toByteArray(),java.nio.charset.StandardCharsets.UTF_8)));}throw e;}finally{finished.set(true);child.destroy();try{child.getOutputStream().close();child.getInputStream().close();}catch(IOException ignored){}}
    }
    private static final Set<String> READY=new HashSet<>();
    public static synchronized void preflight(Context context,String tool,DiskTools.Progress p,Cancelled cancel)throws IOException{
        if(READY.contains(tool))return;
        BlockDevice probe=new BlockDevice(){public int blockSize(){return 512;}public long blockCount(){return 131072;}public void read(long at,byte[] b,int n)throws IOException{throw new IOException("ENGINE_PROBE_IO");}public void write(long at,byte[] b,int n)throws IOException{throw new IOException("ENGINE_PROBE_IO");}public void sync()throws IOException{throw new IOException("ENGINE_PROBE_IO");}public void close(){}};
        boolean udf=tool.equals("mkfs_udf")||tool.equals("udfinfo"),hfs=tool.equals("mkfs_hfsplus")||tool.equals("fsck_hfsplus"),apfs=tool.equals("mkapfs")||tool.equals("apfsck");
        run(context,tool,Arrays.asList(tool.equals("btrfs")?"--version":apfs?"-v":udf||hfs||tool.equals("fsck_fat")||tool.equals("fsck_exfat")?"--help":"-V"),probe,false,p,cancel,tool.equals("fsck_exfat")?16:udf||hfs?1:0);READY.add(tool);
    }
    public static void format(Context context,BlockDevice volume,String fs,String label,int cluster,DiskTools.Progress p,Cancelled cancel)throws IOException{
        if(fs.equals("ntfs")){
            if(label.isEmpty()||label.length()>32||label.indexOf('\0')>=0)throw new IOException("NTFS_LABEL");
            List<String> args=new ArrayList<>(Arrays.asList("-Q","-F","-s","512","-p","2048","-L",label));if(cluster!=0){args.add("-c");args.add(Integer.toString(cluster*512));}args.add("/dev/usbworkbench");try(VerifiedWriteBuffer buffered=new VerifiedWriteBuffer(volume,cancel)){run(context,"mkntfs",args,buffered,true,p,cancel);buffered.finish();}
        }else if(Arrays.asList("ext2","ext3","ext4").contains(fs)){
            if(label.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>16)throw new IOException("EXT_LABEL");long size=volume.blockCount()*volume.blockSize();int inodes=(int)Math.max(1024,Math.min(262144,size/16384));
            List<String> args=new ArrayList<>(Arrays.asList("-F","-t",fs,"-b","4096","-m","0","-N",Integer.toString(inodes),"-L",label,"-E","nodiscard,lazy_itable_init=0,lazy_journal_init=0"));if(!fs.equals("ext2")){args.add("-J");args.add("size="+Math.max(4,Math.min(32,size/(32*1048576))));}args.add("/dev/usbworkbench");run(context,"mke2fs",args,volume,true,p,cancel);
        }else if(fs.equals("f2fs")){if(cluster!=0)throw new IOException("F2FS_CLUSTER");run(context,"mkfs_f2fs",Arrays.asList("-f","-t","0","-R","0:0","-l",label,"/dev/usbworkbench"),volume,true,p,cancel);
        }else throw new IOException("FORMAT_OPTIONS");
    }
}
