package org.usbworkbench;
import android.content.*;import android.database.Cursor;import android.net.Uri;import android.os.ParcelFileDescriptor;import android.provider.DocumentsContract;import android.system.*;
import java.io.*;import java.nio.*;import java.nio.channels.*;import java.util.*;

/** Local seekable document sources and newly-owned output trees. */
final class SafImages {
 static OpticalImage.Source open(Context context,Uri uri,IoRegistry registry)throws IOException {
  ParcelFileDescriptor fd=context.getContentResolver().openFileDescriptor(uri,"r");if(fd==null)throw new IOException("SOURCE_OPEN");
  ParcelFileDescriptor.AutoCloseInputStream input=new ParcelFileDescriptor.AutoCloseInputStream(fd);InputStream tracked=registry.input(input);FileChannel channel=input.getChannel();
  try {long size=channel.size();channel.position(0);if(size<1)throw new IOException("IMAGE_SEEK_REQUIRED");return new OpticalImage.Source(){public long size()throws IOException{return channel.size();}public void read(long at,byte[] b,int off,int n)throws IOException{if(at<0||n<0||at>size()||n>size()-at)throw new IOException("IMAGE_BOUNDS");ByteBuffer buffer=ByteBuffer.wrap(b,off,n);while(buffer.hasRemaining()){int count=channel.read(buffer,at);if(count<=0)throw new IOException("IMAGE_SHORT");at+=count;}}public void close()throws IOException{tracked.close();}};
  }catch(Exception e){tracked.close();throw new IOException("IMAGE_SEEK_REQUIRED",e);}
 }
 static OpticalImage.Source slice(OpticalImage.Source source,OpticalImage.Entry entry){return new OpticalImage.Source(){public long size(){return entry.size;}public void read(long at,byte[] b,int off,int n)throws IOException{OpticalImage.read(source,entry,at,b,off,n);}public void close(){}};}
 static final class Tree implements OpticalImage.Sink {
  final WorkbenchApp app;final Uri root;final Map<String,Uri> dirs=new HashMap<>(),files=new HashMap<>();final List<Uri> created=new ArrayList<>();
  Tree(WorkbenchApp app,Uri root){this.app=app;this.root=root;dirs.put("",root);}
  public void directory(String path)throws IOException {String current="";for(String part:path.split("/")){String next=current.isEmpty()?part:current+"/"+part;if(!dirs.containsKey(next)){app.ensureNameAvailable(dirs.get(current),part);Uri uri=DocumentsContract.createDocument(app.getContentResolver(),dirs.get(current),DocumentsContract.Document.MIME_TYPE_DIR,part);if(uri==null)throw new IOException("TARGET_OPEN");dirs.put(next,uri);created.add(uri);}current=next;}}
  Uri create(String path)throws IOException {ZipTools.safePath(path);int slash=path.lastIndexOf('/');String parent=slash<0?"":path.substring(0,slash),name=slash<0?path:path.substring(slash+1);if(!parent.isEmpty())directory(parent);Uri dir=dirs.get(parent);app.ensureNameAvailable(dir,name);Uri uri=DocumentsContract.createDocument(app.getContentResolver(),dir,"application/octet-stream",name);if(uri==null)throw new IOException("TARGET_OPEN");created.add(uri);files.put(path,uri);return uri;}
  public OutputStream file(String path)throws IOException{return app.output(create(path));}
  public InputStream reopen(String path)throws IOException{Uri uri=files.get(path);if(uri==null)throw new IOException("TARGET_OPEN");return app.input(uri);}
  void rollback(){for(int i=created.size()-1;i>=0;i--)try{Uri uri=created.get(i);if(dirs.containsValue(uri)&&!empty(app,uri))continue;DocumentsContract.deleteDocument(app.getContentResolver(),uri);}catch(Exception ignored){}}
 }
 static boolean empty(Context context,Uri dir)throws IOException {
  Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));
  try(Cursor c=context.getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},null,null,null)){if(c==null)throw new IOException("NO_FOLDER");return !c.moveToFirst();}
 }
 static void removableRoot(Uri uri)throws IOException {
  if(!"com.android.externalstorage.documents".equals(uri.getAuthority()))throw new IOException("WINDOWS_USB_ROOT");
  String id=DocumentsContract.getDocumentId(uri);if(!id.matches("(?i)[0-9a-f]{4}-[0-9a-f]{4}:"))throw new IOException("WINDOWS_USB_ROOT");
 }
 static WimFiles.Store wimStore(WorkbenchApp app,Tree tree,IoRegistry registry,String parent){return wimStore(app,tree,registry,parent,null);}
 static WimFiles.Store wimStore(WorkbenchApp app,Tree tree,IoRegistry registry,String parent,String exportName){return new WimFiles.Store(){
  public void capacity(long n)throws IOException{app.progress(0,-1);StorageGuard.require(app,tree.root,n);}
  public WimFiles.RandomFile create(String name)throws IOException {
   if(exportName!=null&&!name.equals("export.bin"))throw new IOException("FILE_PATH");String output=exportName==null?name:exportName;
   Uri uri=tree.create(parent.isEmpty()?output:parent+"/"+output);ParcelFileDescriptor fd=app.getContentResolver().openFileDescriptor(uri,"rw");if(fd==null)throw new IOException("TARGET_OPEN");Closeable tracked=registry.track(fd);
   return new WimFiles.RandomFile(){private IOException error(Exception e){return new IOException("TARGET_SEEK_REQUIRED",e);}public long size()throws IOException{try{return Os.fstat(fd.getFileDescriptor()).st_size;}catch(ErrnoException e){throw error(e);}}
    public int read(long at,byte[] b)throws IOException{try{int done=0;while(done<b.length){int n=Os.pread(fd.getFileDescriptor(),b,done,b.length-done,at+done);if(n<=0)break;done+=n;}return done;}catch(ErrnoException e){throw error(e);}}
    public void write(long at,byte[] b)throws IOException{try{int done=0;while(done<b.length){int n=Os.pwrite(fd.getFileDescriptor(),b,done,b.length-done,at+done);if(n<=0)throw new IOException("TARGET_STALLED");done+=n;}}catch(ErrnoException e){throw error(e);}}
    public void truncate(long n)throws IOException{try{Os.ftruncate(fd.getFileDescriptor(),n);}catch(ErrnoException e){throw error(e);}}public void sync()throws IOException{try{Os.fsync(fd.getFileDescriptor());}catch(ErrnoException e){throw error(e);}}public void close()throws IOException{tracked.close();}
   };
  }
 };}
}
