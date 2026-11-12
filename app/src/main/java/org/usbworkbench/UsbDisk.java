package org.usbworkbench;

import android.hardware.usb.*;
import java.io.IOException;
import java.nio.*;
import java.util.Arrays;

/** USB BOT/transparent SCSI, LUN0. Kernel-driver release requires explicit per-job consent. */
public final class UsbDisk implements BlockDevice {
    private final UsbDeviceConnection connection;
    private final UsbInterface iface;
    private final UsbEndpoint in, out;
    private int tag=1, sectorSize;
    private long sectors;
    private String inquiryIdentity="",usbSpecification="";
    public String usbSpecification(){return usbSpecification;}
    static String descriptorVersion(byte[] raw){if(raw==null||raw.length<18||(raw[0]&255)<18||raw[1]!=1)return "";int v=(raw[2]&255)|((raw[3]&255)<<8);for(int shift=0;shift<16;shift+=4)if(((v>>>shift)&15)>9)return "";return String.format(java.util.Locale.ROOT,"%x.%02x",v>>>8,v&255);}
    public String inquiryIdentity(){return inquiryIdentity;}
    public static final int MAX_IO=1048576;
    private boolean closed;
    private boolean cacheFlushSupported=true;
    public boolean cacheFlushSupported(){return cacheFlushSupported;}
    private static final class StatusFailure extends IOException {final int status;final long residue;StatusFailure(int status,long residue){this.status=status;this.residue=residue;}}
    private static final class SenseFailure extends IOException {final int key,asc,ascq;SenseFailure(int key,int asc,int ascq,int status,long residue){super((key==7?"SCSI_WRITE_PROTECTED":key==2&&asc==0x3a?"USB_NO_MEDIA":"SCSI_ERROR")+" status="+status+" key="+key+" asc="+asc+" ascq="+ascq+" residue="+residue);this.key=key;this.asc=asc;this.ascq=ascq;}}
    public static UsbInterface storageInterface(UsbDevice device) {
        for(int i=0;i<device.getInterfaceCount();i++) {
            UsbInterface f=device.getInterface(i);
            if(f.getInterfaceClass()==8 && f.getInterfaceSubclass()==6 && f.getInterfaceProtocol()==80) return f;
        }
        return null;
    }
    public UsbDisk(UsbManager manager, UsbDevice device) throws IOException {
        this(manager,device,false);
    }
    public UsbDisk(UsbManager manager, UsbDevice device,boolean allowKernelDetach) throws IOException {
        iface=storageInterface(device);
        if(iface==null) throw new IOException("UNSUPPORTED_USB");
        UsbEndpoint input=null, output=null;
        for(int i=0;i<iface.getEndpointCount();i++) {
            UsbEndpoint e=iface.getEndpoint(i);
            if(e.getType()==UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if(e.getDirection()==UsbConstants.USB_DIR_IN) input=e; else output=e;
            }
        }
        if(input==null||output==null) throw new IOException("UNSUPPORTED_USB");
        in=input;out=output;
        connection=manager.openDevice(device);
        if(connection==null) throw new IOException("USB_PERMISSION");
        if(!connection.claimInterface(iface,false)&&(!allowKernelDetach||!connection.claimInterface(iface,true))) {
            connection.close();throw new IOException(allowKernelDetach?"USB_CLAIM_FAILED":"USB_DIRECT_ACCESS_REQUIRED");
        }
        try {
            try{usbSpecification=descriptorVersion(connection.getRawDescriptors());}catch(RuntimeException unavailable){usbSpecification="";}
            byte[] lun=new byte[1];
            int n=connection.controlTransfer(0xa1,0xfe,0,iface.getId(),lun,1,3000);
            if(n==1 && lun[0]!=0) throw new IOException("MULTI_LUN");
            byte[] inquiry=new byte[36];initialRead(new byte[]{0x12,0,0,0,36,0},inquiry);
            inquiryIdentity=new String(inquiry,8,8,java.nio.charset.StandardCharsets.US_ASCII).trim()+" / "+new String(inquiry,16,16,java.nio.charset.StandardCharsets.US_ASCII).trim()+" / "+new String(inquiry,32,4,java.nio.charset.StandardCharsets.US_ASCII).trim();
            if((inquiry[0]&0x1f)!=0) throw new IOException("UNSUPPORTED_USB");
            // Unit attention after connecting can clear on REQUEST SENSE. Do not retry writes.
            byte[] capacity=new byte[8];
            initialRead(cdb(0x25),capacity);
            ByteBuffer b=ByteBuffer.wrap(capacity).order(ByteOrder.BIG_ENDIAN);
            long last=(b.getInt() & 0xffffffffL);sectorSize=b.getInt();
            if(last==0xffffffffL || (sectorSize!=512 && sectorSize!=4096)) throw new IOException("UNSUPPORTED_CAPACITY");
            sectors=last+1;
            if(sectors<1) throw new IOException("UNSUPPORTED_CAPACITY");
        } catch(Exception e){close(); if(e instanceof IOException) throw (IOException)e;throw new IOException(e);}
    }
    private static byte[] cdb(int op){byte[] c=new byte[10];c[0]=(byte)op;return c;}
    private void transfer(UsbEndpoint endpoint,byte[] bytes,int len) throws IOException {
        int offset=0;
        while(offset<len){int n=connection.bulkTransfer(endpoint,bytes,offset,Math.min(16384,len-offset),15000);if(n<=0)throw new IOException("USB_TRANSFER");offset+=n;}
    }
    private void initialRead(byte[] cdb,byte[] data)throws IOException{
        for(int attempt=0;;attempt++)try{command(cdb,data,true);return;}catch(SenseFailure e){if(attempt>=2||e.key!=6&&!(e.key==2&&e.asc==4))throw e;try{Thread.sleep(150);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new IOException("CANCELLED");}}
    }
    private synchronized void command(byte[] cdb,byte[] data,boolean reading)throws IOException{
        try{execute(cdb,data,reading);}catch(StatusFailure status){
            if(status.status==2)throw new IOException("USB_PHASE_ERROR");
            byte[] sense=new byte[18];int key=0,asc=0,ascq=0;
            if((cdb[0]&255)!=3)try{execute(new byte[]{3,0,0,0,18,0},sense,true);int response=sense[0]&127;if(response==0x70||response==0x71){key=sense[2]&15;asc=sense[12]&255;ascq=sense[13]&255;}else if(response==0x72||response==0x73){key=sense[1]&15;asc=sense[2]&255;ascq=sense[3]&255;}}catch(IOException ignored){}
            throw new SenseFailure(key,asc,ascq,status.status,status.residue);
        }
    }
    private void execute(byte[] cdb,byte[] data,boolean reading) throws IOException {
        int id=tag++;int len=data==null?0:data.length;
        byte[] cbw=new byte[31];ByteBuffer b=ByteBuffer.wrap(cbw).order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(0x43425355).putInt(id).putInt(len).put((byte)(reading?0x80:0)).put((byte)0).put((byte)cdb.length).put(cdb);
        transfer(out,cbw,31);
        if(len>0)transfer(reading?in:out,data,len);
        byte[] status=new byte[13];transfer(in,status,13);
        ByteBuffer s=ByteBuffer.wrap(status).order(ByteOrder.LITTLE_ENDIAN);
        int signature=s.getInt(), returned=s.getInt();long residue=(s.getInt() & 0xffffffffL);int code=status[12]&255;
        if(signature!=0x53425355||returned!=id||residue>len)throw new IOException("USB_PROTOCOL");
        if(code>2)throw new IOException("USB_PROTOCOL");
        if(code!=0||residue!=0)throw new StatusFailure(code,residue);
    }
    public int blockSize(){return sectorSize;} public long blockCount(){return sectors;}
    private void io(long lba,byte[] data,int length,boolean reading)throws IOException{
        if(closed)throw new IOException("USB_CLOSED");
        if(length<=0||length>MAX_IO||length%sectorSize!=0||length>data.length||lba<0||lba>sectors||length/sectorSize>sectors-lba)throw new IOException("DISK_BOUNDS");
        byte[] c=cdb(reading?0x28:0x2a);ByteBuffer.wrap(c).order(ByteOrder.BIG_ENDIAN).putInt(2,(int)lba);int count=length/sectorSize;c[7]=(byte)(count>>>8);c[8]=(byte)count;
        byte[] exact=length==data.length?data:Arrays.copyOf(data,length);command(c,exact,reading);if(reading&&exact!=data)System.arraycopy(exact,0,data,0,length);
    }
    public void read(long lba,byte[] data,int length)throws IOException{io(lba,data,length,true);}
    public void write(long lba,byte[] data,int length)throws IOException{io(lba,data,length,false);}
    public void sync()throws IOException{if(!cacheFlushSupported)return;try{command(cdb(0x35),null,false);}catch(SenseFailure e){if(e.key==5&&(e.asc==0x20||e.asc==0x24)){cacheFlushSupported=false;return;}throw e;}}
    public synchronized void close(){if(closed)return;closed=true;try{connection.releaseInterface(iface);}finally{connection.close();}}
}
