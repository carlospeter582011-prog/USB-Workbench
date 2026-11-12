package android.hardware.usb;
import java.nio.*;
import java.util.*;
public class UsbDeviceConnection {
    public boolean claim=true,closed=false,force=false,passiveBusy=false,badTag=false,badResidue=false;public int claimCalls=0,lun=0,sector=512,statusCode=0,senseKey=0,senseAsc=0,writes=0,maxPacket=16384;public long count=1048576;public int lastLba,lastBlocks;
    private int tag,remaining,op,at;private byte[] payload;private boolean csw;
    public byte[] getRawDescriptors(){byte[] b=new byte[18];b[0]=18;b[1]=1;b[3]=2;return b;}
    public boolean claimInterface(UsbInterface i,boolean f){force=f;claimCalls++;return claim&&(!passiveBusy||f);}
    public boolean releaseInterface(UsbInterface i){return true;}public void close(){closed=true;}
    public int controlTransfer(int a,int b,int c,int d,byte[] bytes,int n,int timeout){bytes[0]=(byte)lun;return 1;}
    public int bulkTransfer(UsbEndpoint ep,byte[] b,int offset,int len,int timeout){
        if(ep.getDirection()==0&&remaining==0&&!csw){
            if(len!=31)throw new AssertionError("CBW must be 31 bytes");ByteBuffer cbw=ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);if(cbw.getInt()!=0x43425355)throw new AssertionError("CBW signature");tag=cbw.getInt();remaining=cbw.getInt();op=b[15]&255;at=0;payload=new byte[remaining];
            if(op==0x25)ByteBuffer.wrap(payload).putInt((int)(count-1)).putInt(sector);
            if(op==3){payload[0]=0x70;payload[2]=(byte)senseKey;payload[12]=(byte)senseAsc;}
            if(op==0x28){lastLba=ByteBuffer.wrap(b).getInt(17);lastBlocks=((b[22]&255)<<8)|(b[23]&255);Arrays.fill(payload,(byte)0x42);}
            if(op==0x2a){writes++;lastLba=ByteBuffer.wrap(b).getInt(17);lastBlocks=((b[22]&255)<<8)|(b[23]&255);}
            csw=remaining==0;return len;
        }
        if(remaining>0){int n=Math.min(Math.min(len,maxPacket),remaining);if(ep.getDirection()==128)System.arraycopy(payload,at,b,offset,n);else System.arraycopy(b,offset,payload,at,n);remaining-=n;at+=n;if(remaining==0)csw=true;return n;}
        if(csw){if(ep.getDirection()!=128||len!=13)throw new AssertionError("CSW framing");ByteBuffer.wrap(b,offset,len).order(ByteOrder.LITTLE_ENDIAN).putInt(0x53425355).putInt(badTag?tag+1:tag).putInt(badResidue?1:0).put((byte)(op==3?0:statusCode));csw=false;return 13;}
        throw new AssertionError("Unexpected transaction");
    }
}
