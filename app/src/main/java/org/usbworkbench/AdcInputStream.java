package org.usbworkbench;
import java.io.*;
/** Original bounded streaming Apple Data Compression decoder; 64 KiB back-reference window. */
public final class AdcInputStream extends InputStream {
 private final InputStream input;private final byte[] window=new byte[65536];private long written;private int remaining,distance;private boolean literal;
 public AdcInputStream(InputStream input){this.input=input;}
 private int required()throws IOException{int b=input.read();if(b<0)throw new IOException("DMG_DATA");return b;}
 @Override public int read()throws IOException{
  if(remaining==0){int command=input.read();if(command<0)return -1;literal=(command&128)!=0;if(literal)remaining=(command&127)+1;else{if((command&64)!=0){remaining=(command&63)+4;distance=(required()<<8|required())+1;}else{remaining=(command>>>2)+3;distance=((command&3)<<8|required())+1;}if(distance>written)throw new IOException("DMG_DATA");}}
  int value=literal?required():window[(int)((written-distance)&65535)]&255;window[(int)(written&65535)]=(byte)value;written++;remaining--;return value;
 }
 @Override public int read(byte[] b,int off,int n)throws IOException{if(off<0||n<0||off>b.length-n)throw new IndexOutOfBoundsException();if(n==0)return 0;int got=0;while(got<n){int v=read();if(v<0)break;b[off+got++]=(byte)v;}return got==0?-1:got;}
 @Override public void close()throws IOException{input.close();}
}
