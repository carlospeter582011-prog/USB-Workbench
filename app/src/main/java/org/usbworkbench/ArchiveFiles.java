package org.usbworkbench;
import java.io.*;import java.util.*;
/** Single-pass archive outputs. Paths are mapped from a Java-validated manifest, never native paths. */
public final class ArchiveFiles implements FileProtocol.Backend,Closeable {
 public interface NamedStore extends WimFiles.Store{void capacity(String name,long additional)throws IOException;}
 private static final class Open{final String name;final WimFiles.RandomFile file;final java.security.MessageDigest hash;Open(String n,WimFiles.RandomFile f){name=n;file=f;try{hash=java.security.MessageDigest.getInstance("SHA-256");}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}}}
 private final Map<String,String> hashes=new HashMap<>();private final OpticalImage.Source source;private final WimFiles.Store store;private final Map<String,Long> allowed,closedSizes=new HashMap<>();private final Map<Integer,Open> handles=new HashMap<>();private int next=1;private boolean closed;
 public ArchiveFiles(OpticalImage.Source s,WimFiles.Store o,Map<String,Long> a){source=s;store=o;allowed=a;}
 private Open get(int id)throws IOException{if(closed)throw new IOException("CANCELLED");Open h=handles.get(id);if(h==null)throw new FileProtocol.FileError(9);return h;}
 public synchronized FileProtocol.Handle open(String name,int flags)throws IOException{
  if(closed)throw new IOException("CANCELLED");if(handles.size()>=4)throw new FileProtocol.FileError(24);int id=next++;if(id<=0)throw new IOException("FILE_PROTOCOL");
  if(name.equals("source.wim")){if((flags&3)!=0||(flags&~(32768|0x80000|0x20000))!=0)throw new FileProtocol.FileError(30);handles.put(id,new Open(name,null));return new FileProtocol.Handle(id,source.size());}
  if(!allowed.containsKey(name)||(flags&3)!=2||(flags&(64|128))!=(64|128)||(flags&~(3|64|128|32768|0x80000|0x20000))!=0)throw new FileProtocol.FileError(13);
  if(closedSizes.containsKey(name))throw new FileProtocol.FileError(17);for(Open h:handles.values())if(h.name.equals(name))throw new FileProtocol.FileError(17);
  store.capacity(0);WimFiles.RandomFile f=store.create(name);handles.put(id,new Open(name,f));return new FileProtocol.Handle(id,f.size());
 }
 public synchronized int read(int id,long offset,byte[] b)throws IOException{Open h=get(id);if(offset<0)throw new IOException("FILE_BOUNDS");long size=size(id);int n=(int)Math.min(b.length,Math.max(0,size-offset));if(n==0)return 0;if(h.file==null){source.read(offset,b,0,n);return n;}return h.file.read(offset,b);}
 public synchronized void write(int id,long offset,byte[] b)throws IOException{Open h=get(id);if(h.file==null)throw new FileProtocol.FileError(30);long max=allowed.get(h.name);if(offset!=h.file.size()||offset<0||offset>max||b.length>max-offset)throw new IOException("ARCHIVE_LIMIT");long additional=Math.max(0,offset+b.length-h.file.size());if(store instanceof NamedStore)((NamedStore)store).capacity(h.name,additional);else store.capacity(additional);h.file.write(offset,b);byte[] check=new byte[b.length];if(h.file.read(offset,check)!=b.length||!Arrays.equals(check,b))throw new IOException("VERIFY_FAILED");h.hash.update(b);}
 public synchronized long size(int id)throws IOException{Open h=get(id);return h.file==null?source.size():h.file.size();}
 public synchronized long pathSize(String name)throws IOException{if(name.equals("source.wim"))return source.size();for(Open h:handles.values())if(h.name.equals(name))return h.file.size();Long n=closedSizes.get(name);if(n!=null)return n;throw new FileProtocol.FileError(2);}
 public synchronized void truncate(int id,long n)throws IOException{throw new FileProtocol.FileError(30);}
 public synchronized void sync(int id)throws IOException{Open h=get(id);if(h.file!=null)h.file.sync();}
 public synchronized void close(int id)throws IOException{Open h=get(id);if(h.file!=null){h.file.sync();closedSizes.put(h.name,h.file.size());StringBuilder hex=new StringBuilder();for(byte b:h.hash.digest())hex.append(String.format(Locale.ROOT,"%02x",b&255));hashes.put(h.name,hex.toString());h.file.close();}handles.remove(id);}
 public synchronized Map<String,String> hashes(){return new HashMap<>(hashes);}
 public synchronized Map<String,Long> outputs(){return new HashMap<>(closedSizes);}
 public synchronized void close()throws IOException{if(closed)return;closed=true;IOException failure=null;for(Open h:handles.values())if(h.file!=null)try{h.file.close();}catch(IOException e){failure=e;}handles.clear();if(failure!=null)throw failure;}
}
