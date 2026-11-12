package org.usbworkbench;
import java.io.IOException;
/** Exact paths, with outer whitespace removed; ambiguous empty/blank components rejected. */
public final class WimPaths {
 public static String normalize(String value,boolean folder)throws IOException{String path=value==null?"":value.trim();if(path.isEmpty())path="/";if(path.equals("/")){if(folder)return path;throw new IOException("WIM_FILE_PATH");}if(path.startsWith("/"))path=path.substring(1);for(String part:path.split("/",-1))if(part.trim().isEmpty())throw new IOException("WIM_FILE_PATH");try{return "/"+ZipTools.safePath(path);}catch(IOException e){throw new IOException("WIM_FILE_PATH",e);}}
}
