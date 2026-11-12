package org.usbworkbench;
import java.io.IOException;import java.util.Arrays;
/** Original bounded LZ4 block codec; no frames/dictionaries or third-party implementation code. */
public final class Lz4Block {
 private static int word(byte[] b,int at){return (b[at]&255)|((b[at+1]&255)<<8)|((b[at+2]&255)<<16)|(b[at+3]<<24);}
 private static int extra(byte[] out,int at,int count){while(count>=255){out[at++]=(byte)255;count-=255;}out[at++]=(byte)count;return at;}
 public static final class Encoder {
  private final int[] positions=new int[4096];
  public int encode(byte[] input,byte[] out)throws IOException{
   if(input.length>65536||out.length<input.length+input.length/255+16)throw new IOException("ZSO_DATA");Arrays.fill(positions,-1);int at=0,anchor=0,written=0,n=input.length;
   while(at<=n-12){int hash=(word(input,at)*-1640531535)>>>20,previous=positions[hash];positions[hash]=at;
    if(previous>=0&&previous<at&&at-previous<=65535&&word(input,previous)==word(input,at)){
     int match=4;while(at+match<n-5&&input[previous+match]==input[at+match])match++;int literals=at-anchor;out[written++]=(byte)((Math.min(literals,15)<<4)|Math.min(match-4,15));if(literals>=15)written=extra(out,written,literals-15);System.arraycopy(input,anchor,out,written,literals);written+=literals;int offset=at-previous;out[written++]=(byte)offset;out[written++]=(byte)(offset>>>8);if(match>=19)written=extra(out,written,match-19);
     int end=at+match;for(int i=at+1;i<end&&i<=n-12;i++)positions[(word(input,i)*-1640531535)>>>20]=i;at=end;anchor=end;
    }else at++;
   }
   int literals=n-anchor;out[written++]=(byte)(Math.min(literals,15)<<4);if(literals>=15)written=extra(out,written,literals-15);System.arraycopy(input,anchor,out,written,literals);return written+literals;
  }
 }
 /** Returns compressed bytes consumed; callers validate format-specific alignment padding. */
 public static int decode(byte[] input,int count,byte[] out)throws IOException{
  if(count<1||count>input.length||out.length>65536)throw new IOException("ZSO_DATA");int at=0,written=0;boolean matched=false;
  while(at<count){int token=input[at++]&255,literals=token>>>4;if(literals==15){int value;do{if(at>=count)throw new IOException("ZSO_DATA");value=input[at++]&255;literals+=value;if(literals>out.length-written)throw new IOException("ZSO_DATA");}while(value==255);}
   if(literals>out.length-written||literals>count-at)throw new IOException("ZSO_DATA");System.arraycopy(input,at,out,written,literals);written+=literals;at+=literals;
   if(written==out.length){if(matched&&literals<5)throw new IOException("ZSO_DATA");return at;}
   if(at+2>count||written>out.length-12)throw new IOException("ZSO_DATA");int offset=(input[at]&255)|((input[at+1]&255)<<8);at+=2;if(offset==0||offset>written)throw new IOException("ZSO_DATA");int length=(token&15)+4;if((token&15)==15){int value;do{if(at>=count)throw new IOException("ZSO_DATA");value=input[at++]&255;length+=value;if(length>out.length-written-5)throw new IOException("ZSO_DATA");}while(value==255);}
   if(length>out.length-written-5)throw new IOException("ZSO_DATA");for(int i=0;i<length;i++)out[written+i]=out[written+i-offset];written+=length;matched=true;
  }throw new IOException("ZSO_DATA");
 }
}
