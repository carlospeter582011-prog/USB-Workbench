package org.usbworkbench;
import android.hardware.usb.*;
import java.io.*;
public class UsbDiskTest {
    static int checks=0;static void check(boolean c,String why){checks++;if(!c)throw new AssertionError(why);}
    interface Action{void run()throws Exception;}
    static void fails(String text,Action a)throws Exception{try{a.run();throw new AssertionError("expected "+text);}catch(IOException e){check(e.getMessage().startsWith(text),e.toString());}}
    static UsbDisk open(UsbDeviceConnection c)throws IOException{return new UsbDisk(new UsbManager(c),new UsbDevice());}
    public static void main(String[] args)throws Exception{
        UsbDeviceConnection c=new UsbDeviceConnection();c.maxPacket=128;
        try(UsbDisk d=open(c)){
            check(d.usbSpecification().equals("2.00"),"USB spec from bcdUSB, not firmware");check(UsbDisk.descriptorVersion(new byte[2]).isEmpty(),"short descriptor unknown");check(UsbDisk.descriptorVersion(null).isEmpty(),"missing descriptor unknown");byte[] badDescriptor=c.getRawDescriptors();badDescriptor[2]=(byte)0xfa;check(UsbDisk.descriptorVersion(badDescriptor).isEmpty(),"invalid BCD descriptor unknown");
            check(!c.force,"never force detach kernel driver");check(d.blockSize()==512&&d.blockCount()==1048576,"READ CAPACITY decoding");
            byte[] data=new byte[16384];d.read(400,data,data.length);check(c.lastLba==400&&c.lastBlocks==32,"READ10 LBA and block count");check(data[0]==0x42&&data[16383]==0x42,"partial bulk read assembly");
            d.write(901,data,512);check(c.lastLba==901&&c.lastBlocks==1&&c.writes==1,"WRITE10 trimmed buffer and exact sector count");
            fails("DISK_BOUNDS",()->d.write(0,data,513));fails("DISK_BOUNDS",()->d.read(d.blockCount(),data,512));fails("DISK_BOUNDS",()->d.read(-1,data,512));d.sync();
        }
        check(c.closed,"close releases connection");
        UsbDeviceConnection busy=new UsbDeviceConnection();busy.claim=false;fails("USB_DIRECT_ACCESS_REQUIRED",()->open(busy));check(busy.closed&&!busy.force,"unapproved driver stays attached");
        UsbDeviceConnection kernel=new UsbDeviceConnection();kernel.passiveBusy=true;try(UsbDisk d=new UsbDisk(new UsbManager(kernel),new UsbDevice(),true)){check(kernel.force&&kernel.claimCalls==2,"approved fallback releases kernel driver");byte[] data=new byte[65536];d.read(100,data,data.length);check(kernel.lastBlocks==128&&data[65535]==0x42,"64KiB random benchmark read");d.write(100,data,data.length);check(kernel.lastBlocks==128,"64KiB image/metadata write");byte[] large=new byte[UsbDisk.MAX_IO];d.read(100,large,large.length);check(kernel.lastBlocks==2048&&large[large.length-1]==0x42,"bounded1MiB caller with16KiB USB transactions");fails("DISK_BOUNDS",()->d.read(0,new byte[UsbDisk.MAX_IO+512],UsbDisk.MAX_IO+512));}
        UsbDeviceConnection denied=new UsbDeviceConnection();denied.claim=false;fails("USB_CLAIM_FAILED",()->new UsbDisk(new UsbManager(denied),new UsbDevice(),true));check(denied.closed&&denied.claimCalls==2,"real claim failure distinguished after fallback");
        UsbDeviceConnection lun=new UsbDeviceConnection();lun.lun=1;fails("MULTI_LUN",()->open(lun));check(lun.closed,"unsupported device closes");
        UsbDeviceConnection huge=new UsbDeviceConnection();huge.count=0x100000000L;fails("UNSUPPORTED_CAPACITY",()->open(huge));
        UsbDeviceConnection fourk=new UsbDeviceConnection();fourk.sector=4096;try(UsbDisk d=open(fourk)){check(d.blockSize()==4096,"4k capacity");d.write(0,new byte[4096],4096);check(fourk.lastBlocks==1,"4k WRITE10 count");}
        UsbDeviceConnection broken=new UsbDeviceConnection();try(UsbDisk d=open(broken)){broken.badTag=true;fails("USB_PROTOCOL",()->d.read(0,new byte[512],512));}
        UsbDeviceConnection reject=new UsbDeviceConnection();try(UsbDisk d=open(reject)){reject.statusCode=1;fails("SCSI_ERROR",()->d.write(0,new byte[512],512));check(reject.writes==1,"failed writes never automatically retried");}
        UsbDeviceConnection residue=new UsbDeviceConnection();try(UsbDisk d=open(residue)){residue.badResidue=true;fails("SCSI_ERROR",()->d.read(0,new byte[512],512));}
        UsbDeviceConnection protectedDisk=new UsbDeviceConnection();try(UsbDisk d=open(protectedDisk)){protectedDisk.statusCode=1;protectedDisk.senseKey=7;protectedDisk.senseAsc=0x27;fails("SCSI_WRITE_PROTECTED",()->d.write(0,new byte[512],512));check(protectedDisk.writes==1,"write protected command never retried");}
        UsbDeviceConnection noFlush=new UsbDeviceConnection();try(UsbDisk d=open(noFlush)){noFlush.statusCode=1;noFlush.senseKey=5;noFlush.senseAsc=0x20;d.sync();check(!d.cacheFlushSupported(),"unsupported optional sync does not fail completed write");}
        UsbDevice dev=new UsbDevice();dev.iface.protocol=98;check(UsbDisk.storageInterface(dev)==null,"UAS-only refused");
        System.out.println("PASS: "+checks+" USB BOT/SCSI framing, partial-transfer, bounds and failure checks (fake transport)");
    }
}
