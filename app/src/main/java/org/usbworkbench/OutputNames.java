package org.usbworkbench;
import java.util.*;
/** Friendly local output names; collision suffixes never overwrite existing entries. */
public final class OutputNames {
 public static String stem(String source){String name=source==null?"":source;name=name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}\\u202a-\\u202e\\u2066-\\u2069]","_").trim();String lower=name.toLowerCase(Locale.ROOT);for(String ext:new String[]{".tar.gz",".tar.xz",".tar.bz2",".tar.lzma",".zip",".7z",".rar",".tgz",".txz",".tar",".iso",".udf",".wim",".img",".dmg",".chd"})if(lower.endsWith(ext)){name=name.substring(0,name.length()-ext.length());break;}name=name.replaceAll("^[. ]+|[. ]+$","");if(name.length()>90){name=name.substring(0,90);if(Character.isHighSurrogate(name.charAt(name.length()-1)))name=name.substring(0,name.length()-1);}return name.isEmpty()?"Archive":name;}
 public static String unique(String base,Collection<String> existing){Set<String> names=new HashSet<>();for(String n:existing)if(n!=null)names.add(n.toLowerCase(Locale.ROOT));for(int i=1;i<=10000;i++){String n=base+(i==1?"":" ("+i+")");if(!names.contains(n.toLowerCase(Locale.ROOT)))return n;}throw new IllegalArgumentException("Too many output names");}
}
