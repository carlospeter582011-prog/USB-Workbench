package org.usbworkbench;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import java.io.*;
/** Destination-specific free-space checks. Never scans or deletes personal files. */
// Deliberately budget actual free bytes, without reclaiming other apps caches.
@android.annotation.SuppressLint("UsableSpace")
public final class StorageGuard {
    public static boolean primaryDocument(Uri uri){String authority=uri.getAuthority();if("com.android.providers.downloads.documents".equals(authority))return true;try{return "com.android.externalstorage.documents".equals(authority)&&android.provider.DocumentsContract.getDocumentId(uri).startsWith("primary:");}catch(Exception e){return false;}}
    public static final long RESERVE=64L*1024*1024;
    public static long available(Context c,Uri uri){
        if("file".equals(uri.getScheme())){File f=new File(uri.getPath());while(!f.exists()&&f.getParentFile()!=null)f=f.getParentFile();return f.getUsableSpace();}
        String authority=uri.getAuthority(),id="";
        try{id=DocumentsContract.getDocumentId(uri);}catch(Exception ignored){try{id=DocumentsContract.getTreeDocumentId(uri);}catch(Exception noTree){}}
        if("com.android.externalstorage.documents".equals(authority)&&id.startsWith("primary:"))return Environment.getExternalStorageDirectory().getUsableSpace();
        if("com.android.providers.downloads.documents".equals(authority))return Environment.getExternalStorageDirectory().getUsableSpace();
        if("com.android.externalstorage.documents".equals(authority)&&id.contains(":")){
            String volume=id.substring(0,id.indexOf(':'));if(volume.matches("[A-Za-z0-9-]+")){long space=new File("/storage",volume).getUsableSpace();if(space>0)return space;}
        }
        try(Cursor cursor=c.getContentResolver().query(DocumentsContract.buildRootsUri(authority),new String[]{DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_AVAILABLE_BYTES},null,null,null)){
            long only=-1;int count=0;if(cursor!=null)while(cursor.moveToNext()){count++;long free=cursor.isNull(1)?-1:cursor.getLong(1);only=free;String root=cursor.getString(0);if(root!=null&&!id.isEmpty()&&id.startsWith(root)&&free>=0)return free;}if(count==1)return only;
        }catch(Exception ignored){}
        return -1;
    }
    public static long require(Context c,Uri destination,long bytes)throws IOException{
        long free=available(c,destination);if(free<0)throw new IOException("SPACE_UNKNOWN");if(free<RESERVE||bytes>free-RESERVE)throw new IOException("SPACE_LOW "+Math.max(0,bytes)+" / "+free);return free-RESERVE;
    }
    public static OutputStream monitor(Context c,Uri destination,OutputStream out,long budget){
        return new FilterOutputStream(out){long used=0,last=0;
            private void check(int n)throws IOException{if(n>budget-used)throw new IOException("SPACE_LOW");if(used-last>=4194304){long free=available(c,destination);if(free>=0&&free<RESERVE+n)throw new IOException("SPACE_LOW");last=used;}}
            @Override public void write(int b)throws IOException{check(1);out.write(b);used++;}
            @Override public void write(byte[] b,int off,int n)throws IOException{check(n);out.write(b,off,n);used+=n;}
        };
    }
}
