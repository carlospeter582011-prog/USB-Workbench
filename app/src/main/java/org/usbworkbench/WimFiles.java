package org.usbworkbench;
import java.io.*;
import java.util.*;

/** Permission-checked files for the WIM subprocess. Every output write is read back. */
public final class WimFiles implements FileProtocol.Backend,Closeable {
 public static final long MAX_PART_BYTES=0xffffffffL;
 public interface RandomFile extends Closeable {
  long size()throws IOException;
  int read(long offset,byte[] bytes)throws IOException;
  void write(long offset,byte[] bytes)throws IOException;
  void truncate(long size)throws IOException;
  void sync()throws IOException;
 }
 public interface Store {RandomFile create(String name)throws IOException;void capacity(long additional)throws IOException;}
 private static final class Open {final String name;final boolean write;Open(String n,boolean w){name=n;write=w;}}
 private final OpticalImage.Source source;private final Store store;
 private final Map<Integer,Open> handles=new HashMap<>();
 private final LinkedHashMap<String,RandomFile> outputs=new LinkedHashMap<>();
 private int next=1;private boolean writable=true,closed;
 public WimFiles(OpticalImage.Source source,Store store){this.source=source;this.store=store;}
 private void live()throws IOException{if(closed)throw new IOException("CANCELLED");}
 private Open handle(int id)throws IOException{live();Open h=handles.get(id);if(h==null)throw new FileProtocol.FileError(9);return h;}
 private static void valid(String name)throws IOException{FileProtocol.name(("/usbwb/"+name).getBytes(java.nio.charset.StandardCharsets.US_ASCII));}
 public synchronized FileProtocol.Handle open(String name,int flags)throws IOException{
  live();valid(name);if(handles.size()>=256)throw new FileProtocol.FileError(24);
  int access=flags&3;boolean writing=access==1||access==2;
  if(access==3||(flags&~(3|64|128|512|32768|0x80000|0x20000))!=0)throw new FileProtocol.FileError(22);
  if(name.equals("source.wim")){if(writing||(flags&(64|128|512))!=0)throw new FileProtocol.FileError(30);}
  else {
   if(!writable&&(writing||(flags&(64|128|512))!=0))throw new FileProtocol.FileError(30);
   RandomFile file=outputs.get(name);
   if(file==null){if(!writing||(flags&64)==0)throw new FileProtocol.FileError(2);if(outputs.size()>=FileProtocol.MAX_PARTS)throw new IOException("WIM_PART_LIMIT");store.capacity(0);file=store.create(name);outputs.put(name,file);}
   else if((flags&(64|128))==(64|128))throw new FileProtocol.FileError(17);
   if((flags&512)!=0){if(!writing)throw new FileProtocol.FileError(13);file.truncate(0);}
  }
  int id=next++;if(id<=0)throw new IOException("FILE_PROTOCOL");handles.put(id,new Open(name,writing));return new FileProtocol.Handle(id,pathSize(name));
 }
 public synchronized int read(int id,long offset,byte[] bytes)throws IOException{
  Open h=handle(id);if(offset<0)throw new IOException("FILE_BOUNDS");long size=pathSize(h.name);int n=(int)Math.min(bytes.length,Math.max(0,size-offset));if(n==0)return 0;
  if(h.name.equals("source.wim")){source.read(offset,bytes,0,n);return n;}
  byte[] buffer=n==bytes.length?bytes:new byte[n];int count=outputs.get(h.name).read(offset,buffer);if(count<0||count>n)throw new IOException("FILE_PROTOCOL");if(buffer!=bytes)System.arraycopy(buffer,0,bytes,0,count);return count;
 }
 public synchronized void write(int id,long offset,byte[] bytes)throws IOException{
  Open h=handle(id);if(!writable||!h.write||h.name.equals("source.wim"))throw new FileProtocol.FileError(30);
  if(offset<0||offset>MAX_PART_BYTES||bytes.length>MAX_PART_BYTES-offset)throw new IOException("FILE_BOUNDS");
  RandomFile file=outputs.get(h.name);store.capacity(Math.max(0,offset+bytes.length-file.size()));file.write(offset,bytes);
  byte[] check=new byte[bytes.length];int n=file.read(offset,check);if(n!=bytes.length||!Arrays.equals(bytes,check))throw new IOException("VERIFY_FAILED");
 }
 public synchronized long size(int id)throws IOException{return pathSize(handle(id).name);}
 public synchronized long pathSize(String name)throws IOException{live();valid(name);if(name.equals("source.wim"))return source.size();RandomFile f=outputs.get(name);if(f==null)throw new FileProtocol.FileError(2);return f.size();}
 public synchronized void truncate(int id,long size)throws IOException{Open h=handle(id);if(!writable||!h.write||h.name.equals("source.wim"))throw new FileProtocol.FileError(30);if(size<0||size>MAX_PART_BYTES)throw new IOException("FILE_BOUNDS");RandomFile f=outputs.get(h.name);store.capacity(Math.max(0,size-f.size()));f.truncate(size);}
 public synchronized void sync(int id)throws IOException{Open h=handle(id);if(!h.name.equals("source.wim"))outputs.get(h.name).sync();}
 public synchronized void close(int id)throws IOException{Open h=handle(id);if(h.write)sync(id);handles.remove(id);}
 public synchronized List<String> names(){return new ArrayList<>(outputs.keySet());}
 public synchronized void seal()throws IOException{live();for(RandomFile file:outputs.values())file.sync();handles.clear();writable=false;}
 public synchronized void close()throws IOException{if(closed)return;closed=true;IOException failure=null;for(RandomFile file:outputs.values())try{file.close();}catch(IOException e){failure=e;}handles.clear();try{source.close();}catch(IOException e){failure=e;}if(failure!=null)throw failure;}
}
