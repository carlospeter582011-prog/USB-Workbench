package org.usbworkbench;
import android.content.Context;import java.io.*;import java.util.*;
/** Creates an empty persistence filesystem, never a Linux distribution or system image. */
public final class PersistenceImage {
 public static long size(int mebibytes,String label)throws IOException{
  if(mebibytes<256||mebibytes>16384||label==null||!label.matches("[A-Za-z0-9_-]{1,16}"))throw new IOException("PERSISTENCE_OPTIONS");
  return mebibytes*1048576L;
 }
 public static void create(Context c,BlockDevice target,String label,boolean config,DiskTools.Progress p,NativeTools.Cancelled cancel)throws IOException{
  if(target.blockSize()!=512||target.blockCount()<524288||target.blockCount()>33554432L||label==null||!label.matches("[A-Za-z0-9_-]{1,16}"))throw new IOException("PERSISTENCE_OPTIONS");
  NativeTools.preflight(c,"mke2fs",p,cancel);NativeTools.preflight(c,"e2fsck",p,cancel);
  File seed=new File(c.getFilesDir(),"persistence-seed-"+System.nanoTime());if(!seed.mkdir())throw new IOException("TARGET_OPEN");
  try{if(config)try(OutputStream out=new FileOutputStream(new File(seed,"persistence.conf"))){out.write("/ union\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));}
   long size=target.blockCount()*512;int inodes=(int)Math.max(4096,Math.min(262144,size/16384));
   NativeTools.run(c,"mke2fs",Arrays.asList("-F","-t","ext4","-b","4096","-m","0","-N",Integer.toString(inodes),"-L",label,"-E","nodiscard,lazy_itable_init=0,lazy_journal_init=0,no_copy_xattrs,root_owner=0:0,root_perms=0755","-J","size=8","-d",seed.getAbsolutePath(),"/dev/usbworkbench"),target,true,p,cancel);
   NativeTools.run(c,"e2fsck",Arrays.asList("-f","-n","/dev/usbworkbench"),target,false,p,cancel);target.sync();
  }finally{new File(seed,"persistence.conf").delete();seed.delete();}
 }
}
