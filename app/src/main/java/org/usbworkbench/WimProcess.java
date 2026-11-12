package org.usbworkbench;
import java.io.*;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;

/** Fixed bundled executable; no shell or downloaded executable. Separate file RPC. */
public final class WimProcess {
 public interface Cancelled {boolean get();}
 public static boolean available(File dir){return new File(dir,"libwimlib_imagex.so").isFile()&&new File(dir,"libfile_bridge.so").isFile();}
 public static String run(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled)throws IOException{
  return run(dir,args,files,progress,cancelled,false);
 }
 public static String run(File dir,List<String> args,FileProtocol.Backend files,DiskTools.Progress progress,Cancelled cancelled,boolean export)throws IOException{
  if(!available(dir))throw new IOException("ENGINE_MISSING");
  List<String> command=new ArrayList<>();command.add(new File(dir,"libwimlib_imagex.so").getAbsolutePath());command.addAll(args);
  ProcessBuilder builder=new ProcessBuilder(command);Map<String,String> env=builder.environment();env.remove("LD_LIBRARY_PATH");env.put("LD_PRELOAD",new File(dir,"libfile_bridge.so").getAbsolutePath());env.put("USBWB_FILES","1");if(export)env.put("USBWB_EXPORT_STDOUT","1");else env.remove("USBWB_EXPORT_STDOUT");env.put("LC_ALL","C.UTF-8");env.put("LANG","C.UTF-8");
  Process child=builder.start();AtomicBoolean finished=new AtomicBoolean(),timedOut=new AtomicBoolean();ByteArrayOutputStream log=new ByteArrayOutputStream();
  Thread stderr=new Thread(()->{try(InputStream in=child.getErrorStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)synchronized(log){int keep=Math.min(n,65536-log.size());if(keep>0)log.write(b,0,keep);}}catch(IOException ignored){}},"wim-log");stderr.setDaemon(true);stderr.start();
  Thread watch=new Thread(()->{long begin=System.nanoTime();while(!finished.get()){if(cancelled.get()||System.nanoTime()-begin>2*60*60*1000000000L){timedOut.set(!cancelled.get());child.destroy();try{child.getInputStream().close();}catch(IOException ignored){}return;}try{Thread.sleep(100);}catch(InterruptedException e){return;}}},"wim-watch");watch.setDaemon(true);watch.start();
  try{FileProtocol.serve(child.getInputStream(),child.getOutputStream(),files,progress);int code;try{code=child.waitFor();stderr.join(2000);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("CANCELLED");}if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");String text;synchronized(log){text=new String(log.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);}if(code!=0)throw new IOException("WIM_ENGINE_FAILED "+code+"\n"+text);return text;
  }catch(IOException e){if(cancelled.get())throw new IOException("CANCELLED");if(timedOut.get())throw new IOException("ENGINE_TIMEOUT");throw e;}finally{finished.set(true);child.destroy();try{child.getOutputStream().close();child.getInputStream().close();}catch(IOException ignored){}}
 }
 public static void split(File dir,WimFiles files,DiskTools.Progress p,Cancelled cancel,int megabytes)throws IOException{
  if(megabytes<1||megabytes>3800)throw new IOException("WIM_PART_SIZE");
  run(dir,Arrays.asList("split","/usbwb/source.wim","/usbwb/install.swm",Integer.toString(megabytes),"--check","--quiet"),files,p,cancel);
  files.seal();List<String> args=new ArrayList<>(Arrays.asList("verify","/usbwb/install.swm","--quiet"));for(String name:files.names())if(!name.equals("install.swm"))args.add("--ref=/usbwb/"+name);
  run(dir,args,files,p,cancel);
 }
}
