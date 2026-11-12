package org.usbworkbench;
import java.io.*;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;

/** Fixed bundled executable; no shell or downloaded executable. Separate file RPC. */
public final class ArchiveProcess {
 public interface Cancelled {boolean get();}
 public static boolean available(File dir){return new File(dir,"libusbwb_archive.so").isFile()&&new File(dir,"libfile_bridge.so").isFile();}
 public static String run(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled)throws IOException{
  return run(dir,args,files,progress,cancelled,false);
 }
 public static String run(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled,boolean export)throws IOException{return engine(dir,args,files,progress,cancelled,export,0,"");}
 public static String chd(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled)throws IOException{return engine(dir,args,files,progress,cancelled,false,1,"");}
 public static String filesystem(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled,String kind)throws IOException{if(!kind.equals("ntfs")&&!kind.equals("ext"))throw new IOException("FS_UNSUPPORTED");return engine(dir,args,files,progress,cancelled,false,kind.equals("ntfs")?2:3,"");}
 public static String password(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress p,Cancelled c,String password)throws IOException{if(password==null||password.length()>256||password.indexOf('\0')>=0)throw new IOException("ARCHIVE_PASSWORD");return engine(dir,args,files,p,c,false,0,password);}
 private static String engine(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled,boolean export,int engine,String password)throws IOException{
  boolean chd=engine==1;String tool=engine==0?"libusbwb_archive.so":engine==1?"libusbwb_chd.so":engine==2?"libusbwb_ntfsimage.so":"libusbwb_extimage.so";
  if(!(new File(dir,tool).isFile()&&new File(dir,"libfile_bridge.so").isFile()))throw new IOException("ENGINE_MISSING");
  List<String> command=new ArrayList<>();command.add(new File(dir,tool).getAbsolutePath());command.addAll(args);
  ProcessBuilder builder=new ProcessBuilder(command);Map<String,String> env=builder.environment();env.remove("LD_LIBRARY_PATH");env.put("LD_PRELOAD",new File(dir,"libfile_bridge.so").getAbsolutePath());env.put("USBWB_FILES","1");if(export)env.put("USBWB_EXPORT_STDOUT","1");else env.remove("USBWB_EXPORT_STDOUT");env.put("LC_ALL","C.UTF-8");env.put("LANG","C.UTF-8");
  env.remove("USBWB_ARCHIVE_PASSWORD");if(engine==0&&!password.isEmpty())env.put("USBWB_ARCHIVE_PASSWORD",password);
  Process child=builder.start();env.remove("USBWB_ARCHIVE_PASSWORD");AtomicBoolean finished=new AtomicBoolean(),timedOut=new AtomicBoolean();ByteArrayOutputStream log=new ByteArrayOutputStream();
  Thread stderr=new Thread(()->{try(InputStream in=child.getErrorStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)synchronized(log){int keep=Math.min(n,65536-log.size());if(keep>0)log.write(b,0,keep);}}catch(IOException ignored){}},"archive-log");stderr.setDaemon(true);stderr.start();
  Thread watch=new Thread(()->{long begin=System.nanoTime();while(!finished.get()){if(cancelled.get()||System.nanoTime()-begin>30*60*1000000000L){timedOut.set(!cancelled.get());child.destroy();try{child.getInputStream().close();}catch(IOException ignored){}return;}try{Thread.sleep(100);}catch(InterruptedException e){return;}}},"archive-watch");watch.setDaemon(true);watch.start();
  try{FileProtocol.serve(child.getInputStream(),child.getOutputStream(),files,progress);int code;try{code=child.waitFor();stderr.join(2000);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("CANCELLED");}if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");String text;synchronized(log){text=new String(log.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);}if(!password.isEmpty())text=text.replace(password,"[redacted]");if(code!=0)throw new IOException((chd?"CHD_ENGINE_FAILED ":engine>=2?"FSIMAGE_ENGINE_FAILED ":"ARCHIVE_ENGINE_FAILED ")+code+"\n"+text);return text;
  }catch(IOException e){if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");throw e;}finally{finished.set(true);child.destroy();try{child.getOutputStream().close();child.getInputStream().close();}catch(IOException ignored){}}
 }
}
