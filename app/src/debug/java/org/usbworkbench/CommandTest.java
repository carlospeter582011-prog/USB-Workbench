package org.usbworkbench;
import android.app.*;import android.content.*;import android.hardware.usb.*;import android.os.*;
import org.json.*;import java.io.*;import java.security.*;import java.util.*;
/** Shell instrumentation only in debug builds. Never included in QA/release.
 * probe: no writes. sample: explicit exact identity/geometry, one USB, bounded
 * 4MiB view, original bytes backed up and restored. No formatting or full-disk claims.
 */
public final class CommandTest extends Instrumentation {
 private Bundle args;private JSONObject report=new JSONObject();private JSONArray checks=new JSONArray();
 @Override public void onCreate(Bundle b){super.onCreate(b);args=b==null?new Bundle():b;start();}
 private void check(boolean ok,String name)throws Exception{if(!ok)throw new IOException("CHECK_FAILED: "+name);checks.put(name);}
 private UsbDevice only(UsbManager m)throws IOException{UsbDevice found=null;for(UsbDevice d:m.getDeviceList().values())if(UsbDisk.storageInterface(d)!=null){if(found!=null)throw new IOException("MULTIPLE_STORAGE_DEVICES");found=d;}if(found==null)throw new IOException("NO_USB");return found;}
 private static String sha(byte[] b)throws Exception{StringBuilder s=new StringBuilder();for(byte x:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format(Locale.ROOT,"%02x",x&255));return s.toString();}
 private void identity(UsbManager m,UsbDevice d)throws Exception{report.put("device",d.getDeviceName()).put("vendor",d.getVendorId()).put("product",d.getProductId()).put("permission",m.hasPermission(d));}
 @Override public void onStart(){Bundle output=new Bundle();File saved=null;try{
  Context c=getTargetContext();WorkbenchApp app=(WorkbenchApp)c.getApplicationContext();if(app.running)throw new IOException("JOB_BUSY");
  String op=args.getString("op","probe");if(!op.equals("probe")&&!op.equals("sample"))throw new IOException("UNKNOWN_OPERATION");
  UsbManager manager=(UsbManager)c.getSystemService(Context.USB_SERVICE);UsbDevice d=only(manager);identity(manager,d);report.put("operation",op).put("releaseVersion","0.16.1").put("formatsRetested",false);
  if(!manager.hasPermission(d))throw new IOException("USB_PERMISSION: Select this USB and allow access in the app first");
  boolean sample=op.equals("sample");
  if(sample&&(!"EMPTY_TEST_USB".equals(args.getString("consent"))||!d.getDeviceName().equals(args.getString("device"))||!Integer.toString(d.getVendorId()).equals(args.getString("vendor"))||!Integer.toString(d.getProductId()).equals(args.getString("product"))))throw new IOException("TARGET_IDENTITY_REQUIRED");
  try(UsbDisk disk=new UsbDisk(manager,d,"true".equals(args.getString("detach")))){
   report.put("blocks",disk.blockCount()).put("sectorSize",disk.blockSize()).put("inquiry",disk.inquiryIdentity()).put("capacityBytes",disk.blockCount()*disk.blockSize());
   if(sample){
    if(!Long.toString(disk.blockCount()).equals(args.getString("blocks"))||!Integer.toString(disk.blockSize()).equals(args.getString("sector"))||!disk.inquiryIdentity().equals(args.getString("inquiry")))throw new IOException("TARGET_GEOMETRY_CHANGED");
    final int bytes=4*1048576;if(disk.blockCount()*disk.blockSize()<bytes*2L)throw new IOException("USB_TOO_SMALL");
    BlockDevice view=new SliceDevice(disk,0,bytes/disk.blockSize());ByteArrayOutputStream backup=new ByteArrayOutputStream(bytes);DiskTools.backup(view,backup,(a,b)->{});byte[] original=backup.toByteArray();check(original.length==bytes,"bounded physical backup size");
    saved=File.createTempFile("command-usb-original-",".bin",c.getCacheDir());try(FileOutputStream f=new FileOutputStream(saved)){f.write(original);f.getFD().sync();}report.put("originalSha256",sha(original));
    byte[] pattern=new byte[bytes];new Random(0x5553425742L).nextBytes(pattern);boolean restored=false;
    try{
     UsbDevice now=only(manager);if(!now.getDeviceName().equals(d.getDeviceName())||now.getDeviceId()!=d.getDeviceId())throw new IOException("DEVICE_CHANGED");
     DiskTools.writeImage(view,new ByteArrayInputStream(pattern),bytes,(a,b)->{});DiskTools.verifyImage(view,new ByteArrayInputStream(pattern),bytes,(a,b)->{});check(true,"physical image write and independent source comparison (first4MiB)");
     ByteArrayOutputStream readback=new ByteArrayOutputStream(bytes);DiskTools.backup(view,readback,(a,b)->{});check(Arrays.equals(pattern,readback.toByteArray()),"physical backup matches all patterned bytes");
     DiskTools.wipe(view,(a,b)->{});check(DiskTools.verifyZero(view,(a,b)->{})==bytes,"physical zero-fill and zero verification (first4MiB)");
     DiskTools.writeImage(view,new ByteArrayInputStream(pattern),bytes,(a,b)->{});boolean mismatch=false;try{DiskTools.verifyZero(view,(a,b)->{});}catch(IOException e){mismatch=e.getMessage().startsWith("ZERO_MISMATCH");}check(mismatch,"physical nonzero data correctly rejected by zero checker");
    }finally{
     // Restore only the already-open exact connection; never reopen a replacement drive.
     DiskTools.writeImage(view,new ByteArrayInputStream(original),bytes,(a,b)->{});DiskTools.verifyImage(view,new ByteArrayInputStream(original),bytes,(a,b)->{});restored=true;report.put("originalRestoredAndVerified",true);
     if(restored){check(saved.delete(),"owned USB backup removed after verified restoration");saved=null;}
    }
    report.put("testedBytes",bytes).put("wholeDriveTested",false).put("twoDriveCloneTested",false).put("fullCapacityTested",false);
   }
  }
  report.put("status","pass");
 }catch(Throwable e){try{report.put("status","blocked_or_failed").put("error",e.toString());if(saved!=null)report.put("retainedOriginalBackup",saved.getAbsolutePath());}catch(Exception ignored){}}
 try{report.put("checks",checks);}catch(Exception ignored){}output.putString("stream",report.toString()+"\n");finish("pass".equals(report.optString("status"))?Activity.RESULT_OK:Activity.RESULT_CANCELED,output);
 }
}
