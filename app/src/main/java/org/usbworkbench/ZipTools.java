package org.usbworkbench;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** ZIP extraction into an abstract newly-created sandbox. Symlink metadata is never followed. */
public final class ZipTools {
    public interface Sink {void directory(String path)throws IOException;OutputStream file(String path)throws IOException;}
    public static String safePath(String name)throws IOException{
        if(name==null||name.isEmpty()||name.length()>1024||name.startsWith("/")||name.contains("\\")||name.contains(":"))throw new IOException("ZIP_PATH");
        String clean=name.endsWith("/")?name.substring(0,name.length()-1):name;
        for(String part:clean.split("/",-1)){if(part.isEmpty()||part.equals(".")||part.equals("..")||part.length()>255)throw new IOException("ZIP_PATH");for(int i=0;i<part.length();i++)if(part.charAt(i)<32)throw new IOException("ZIP_PATH");}return clean;
    }
    public static long extract(InputStream source,Sink sink,long maxBytes,int maxEntries,DiskTools.Progress p)throws IOException{
        if(maxBytes<1||maxEntries<1)throw new IllegalArgumentException();long expanded=0;int count=0;Set<String> names=new HashSet<>();byte[] buf=new byte[65536];
        try(ZipInputStream in=new ZipInputStream(source)){
            ZipEntry e;while((e=in.getNextEntry())!=null){p.update(expanded,-1);if(++count>maxEntries)throw new IOException("ZIP_LIMIT");String path=safePath(e.getName());if(!names.add(path.toLowerCase(Locale.ROOT)))throw new IOException("ZIP_DUPLICATE");
                if(e.isDirectory()){if(in.read()!=-1)throw new IOException("ZIP_PATH");sink.directory(path);}
                else{int slash=path.lastIndexOf('/');if(slash>0)sink.directory(path.substring(0,slash));try(OutputStream out=sink.file(path)){int n;while((n=in.read(buf))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");if(n>maxBytes-expanded)throw new IOException("ZIP_LIMIT");p.update(expanded,-1);out.write(buf,0,n);expanded+=n;}out.flush();}}
                in.closeEntry();
            }
        }
        if(count==0)throw new IOException("NOT_ZIP");p.update(expanded,expanded);return expanded;
    }
}
