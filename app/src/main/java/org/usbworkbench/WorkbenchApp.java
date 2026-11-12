package org.usbworkbench;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.hardware.usb.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class WorkbenchApp extends Application {
    public final ExecutorService worker=Executors.newSingleThreadExecutor();
    public volatile String selected="",tree="",currentFolder="",image="",imageName="",result="",error="",job="",status="idle";
    public volatile String phase="",formatOptions="";
    public volatile long imageSize=-1,done=0,total=0,started=0;
    public volatile boolean running=false,cancel=false;
    public final List<Uri> zipInputs=new ArrayList<>();
    private final Map<Uri,String> zipInputNames=new HashMap<>();
    public volatile JSONArray files=new JSONArray(),history=new JSONArray();
    public volatile Runnable pending;
    public volatile boolean usbJob;
    public volatile long deviceEpoch=0;
    private long lastProgress=0;
    private volatile long startedElapsed,phaseElapsed;
    public volatile IoMetrics formatIo;
    private JSONArray cachedHistorySource,cachedHistoryShown;
    private boolean cachedHistoryArabic;
    private synchronized JSONArray localizedHistory(boolean ar){
        JSONArray source=history;if(source==cachedHistorySource&&cachedHistoryShown!=null&&cachedHistoryArabic==ar)return cachedHistoryShown;
        JSONArray shown=new JSONArray();for(int i=0;i<source.length();i++){JSONObject h=source.optJSONObject(i);if(h!=null)try{JSONObject copy=new JSONObject(h.toString());copy.put("displayResult",ReportText.render(copy.optString("result"),ar));copy.put("displayError",ReportText.error(copy.optString("error"),ar));copy.put("displayPhase",ReportText.phase(copy.optString("phase"),ar));shown.put(copy);}catch(JSONException ignored){}}
        cachedHistorySource=source;cachedHistoryArabic=ar;cachedHistoryShown=shown;return shown;
    }
    private final IoRegistry io=new IoRegistry();
    public void cancelNow(){cancel=true;io.cancelAsync();}
    
    public String jobTitle(){
        if(Arrays.asList("writePersistent","readMap","readMapShow","wimTreeList","wimTreeExtract","ulCreate","ulInspect","fsusbList","fsusbExtract","fsimageList","fsimageExtract","verifyZero","dmgRaw","archiveList","archiveExtract","chdRaw","clonePhone","fileBenchmark","persistence","convertTable","wimList","wimExport","ventoyUpdate").contains(job))return ReportText.job(job,arabic());
        switch(job){case "partitionEdit":return tr("Edit partition metadata","تعديل بيانات الأقسام");case "ventoyImport":return tr("Import Ventoy package","استيراد حزمة Ventoy");case "ventoyInstall":return tr("Install Ventoy (experimental)","تثبيت Ventoy (تجريبي)");case "windows":return tr("Prepare Windows UEFI","تجهيز Windows UEFI");case "wimSplit":return tr("Split WIM for FAT32","تقسيم WIM من أجل FAT32");case "opticalList":return tr("Browse ISO/UDF","تصفح ISO/UDF");case "opticalExtract":return tr("Extract ISO/UDF","استخراج ISO/UDF");case "tarList":return tr("Inspect TAR/GZIP/XZ","عرض TAR/GZIP/XZ");case "tarExtract":return tr("Extract TAR/GZIP/XZ","استخراج TAR/GZIP/XZ");case "checksums":return tr("File checksums","بصمات الملفات");case "clone":return tr("Clone USB","نسخ USB");case "randomread":return tr("Random read benchmark","قياس القراءة العشوائية");case "scan":return tr("Read surface scan","فحص سطح القراءة");case "hex":return tr("Sector viewer","عارض القطاعات");case "split":return tr("Split file","تقسيم ملف");case "join":return tr("Join parts","دمج الأجزاء");case "createzip":return tr("Create ZIP","إنشاء ZIP");case "imageinfo":return tr("Image inspector","فحص الصورة");case "diagnostic":return tr("Compatibility check","فحص التوافق");case "report":return tr("Export report","تصدير تقرير");case "capacity":return tr("Full capacity test","اختبار السعة الكامل");case "extract":return tr("Extract ZIP","استخراج ZIP");case "formatExfat":return tr("Format exFAT","تهيئة exFAT");case "write":return tr("Write & verify","كتابة وتحقق");case "verify":return tr("Verify image","التحقق من الصورة");case "format":return tr("Format USB","تهيئة USB");case "wipe":return tr("Zero-fill","الملء بالأصفار");case "backup":return tr("Backup drive","نسخ الوحدة");case "info":return tr("Full USB analyzer","محلل USB الشامل");case "copy":return tr("Copy files","نسخ الملفات");case "hash":return tr("SHA-256 checksum","بصمة SHA-256");case "benchmark":return tr("Read benchmark","قياس سرعة القراءة");case "zip":return tr("Inspect ZIP","عرض ZIP");default:return job;}
    }
    public UsbManager usb(){return (UsbManager)getSystemService(USB_SERVICE);}
    public boolean arabic(){return getSharedPreferences("app",0).getBoolean("ar",AppLanguage.isArabic(Locale.getDefault().getLanguage()));}
    public String tr(String en,String ar){return arabic()?ar:en;}
    @Override public void onCreate(){super.onCreate();tree=getSharedPreferences("app",0).getString("tree","");try{history=new JSONArray(getSharedPreferences("app",0).getString("history","[]"));}catch(Exception ignored){}
        android.content.SharedPreferences prefs=getSharedPreferences("app",0);
        if(prefs.getBoolean("inflight",false)){job=prefs.getString("inflightJob","interrupted");started=prefs.getLong("inflightStarted",System.currentTimeMillis());status="failed";error="INTERRUPTED";record();}
    }
    public static JSONObject obj(Object... args){JSONObject o=new JSONObject();try{for(int i=0;i<args.length;i+=2)o.put((String)args[i],args[i+1]);}catch(JSONException e){throw new IllegalArgumentException(e);}return o;}
    public UsbDevice target()throws IOException{
        UsbDevice d=usb().getDeviceList().get(selected);
        if(d==null)throw new IOException("NO_DEVICE");if(!usb().hasPermission(d))throw new IOException("USB_PERMISSION");return d;
    }
    public JSONObject snapshot(){
        JSONArray devices=new JSONArray();
        for(UsbDevice d:usb().getDeviceList().values()){
            String name=d.getProductName();if(name==null)name="USB";
            devices.put(obj("id",d.getDeviceName(),"name",name,"vendor",String.format(Locale.ROOT,"%04X:%04X",d.getVendorId(),d.getProductId()),"supported",UsbDisk.storageInterface(d)!=null,"permission",usb().hasPermission(d)));
        }
        JSONArray shown=localizedHistory(arabic());
        JSONArray zipNames=new JSONArray();synchronized(zipInputs){for(Uri u:zipInputs)zipNames.put(zipInputNames.get(u));}
        return obj("zipInputs",zipNames,"ventoyVersion",getSharedPreferences("app",0).getString("ventoyVersion",""),"devices",devices,"selected",selected,"image",imageName,"imageSize",imageSize,"tree",!tree.isEmpty(),"folder",currentFolder,"files",files,"job",job,"status",status,"done",done,"total",total,"result",result,"error",error,"displayResult",ReportText.render(result,arabic()),"displayError",ReportText.error(error,arabic()),"displayPhase",ReportText.phase(phase,arabic()),"formatOptions",formatOptions,"running",running,"cancelRequested",cancel,"elapsedSeconds",running?Math.max(0,(SystemClock.elapsedRealtime()-startedElapsed)/1000):0,"stageSeconds",running?Math.max(0,(SystemClock.elapsedRealtime()-phaseElapsed)/1000):0,"io",ioSnapshot(),"history",shown);
    }
    public synchronized void queue(String name,boolean usbTask,Job work)throws IOException{
        if(running)throw new IOException("JOB_BUSY");
        if(usbTask)target();
        running=true;cancel=false;formatIo=name.equals("format")?new IoMetrics():null;startedElapsed=phaseElapsed=SystemClock.elapsedRealtime();job=name;status="starting";done=total=0;result=error=phase=formatOptions="";started=System.currentTimeMillis();usbJob=usbTask;
        getSharedPreferences("app",0).edit().putBoolean("inflight",true).putString("inflightJob",name).putLong("inflightStarted",started).apply();
        pending=()->{
            status="running";
            try{String text=work.run();if(cancel)throw new IOException("CANCELLED");result=text==null?"":text;status="complete";}
            catch(Exception e){error=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();for(Throwable extra:e.getSuppressed())if(error.length()<65536)error+="\n"+extra.getMessage();status=cancel?"cancelled":"failed";}
            finally{record();}
        };
        try{Intent service=new Intent(this,OperationService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);}
        catch(Exception e){running=false;pending=null;status="failed";error="SERVICE_START";record();throw new IOException("SERVICE_START",e);}
    }
    public interface Job {String run()throws Exception;}
    public void progress(long value,long max)throws IOException{
        if(cancel||Thread.currentThread().isInterrupted())throw new IOException("CANCELLED");
        done=value;total=max;
        long now=SystemClock.elapsedRealtime();if(now-lastProgress>1000){lastProgress=now;OperationService s=OperationService.instance;if(s!=null)s.update();}
    }
    public void formatStage(String value){phase=value;phaseElapsed=SystemClock.elapsedRealtime();done=0;total=-1;OperationService service=OperationService.instance;if(service!=null)service.update();}
    private JSONObject ioSnapshot(){IoMetrics io=formatIo;if(io==null)return null;return obj("readBytes",io.readBytes,"writeBytes",io.writeBytes,"reads",io.reads,"writes",io.writes,"syncs",io.syncs,"operation",io.action,"lastOperation",io.lastAction,"activeMs",io.activeMillis(),"idleMs",io.idleMillis());}
    private String ioReport(JSONObject io){if(io==null)return "";return tr("USB disk data read (includes verification): ","بيانات قرص USB المقروءة (تشمل التحقق): ")+io.optLong("readBytes")+tr(" bytes\nUSB disk data written: "," بايت\nبيانات قرص USB المكتوبة: ")+io.optLong("writeBytes")+tr(" bytes\nCompleted read / write / sync requests: "," بايت\nطلبات القراءة / الكتابة / المزامنة المكتملة: ")+io.optLong("reads")+" / "+io.optLong("writes")+" / "+io.optLong("syncs");}
    public String progressText(){String text=cancel?tr("Stopping; waiting for the current request to finish.","جارٍ الإيقاف؛ انتظار انتهاء الطلب الحالي."):(phase.isEmpty()?"":ReportText.phase(phase,arabic())+" • ")+(total>0?tr("Stage ","المرحلة ")+(int)Math.min(100,100.0*done/total)+"%":tr("Working — percentage unavailable","جارٍ العمل — النسبة غير متاحة"));if(job.equals("format"))text+="\n"+ioReport(ioSnapshot());return text;}
    public synchronized void failStart(){status="failed";error="SERVICE_START";record();pending=null;running=false;}
    private synchronized void record(){
        if(formatIo!=null)formatIo.freeze();
        JSONArray list=new JSONArray();list.put(obj("job",job,"status",status,"time",System.currentTimeMillis(),"result",result,"error",error,"phase",phase,"formatOptions",formatOptions,"bytes",done,"duration",System.currentTimeMillis()-started,"io",ioSnapshot()));for(int i=0;i<Math.min(19,history.length());i++)list.put(history.opt(i));history=list;getSharedPreferences("app",0).edit().putString("history",history.toString()).putBoolean("inflight",false).apply();
    }
    public void pickImage(Uri uri)throws IOException{
        image=uri.toString();imageName=displayName(uri);imageSize=size(uri);
    }
    public String displayName(Uri uri){try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())return c.getString(0);}catch(Exception ignored){}return "image.img";}
    public long size(Uri uri){try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.SIZE},null,null,null)){if(c!=null&&c.moveToFirst()&&!c.isNull(0))return c.getLong(0);}catch(Exception ignored){}return -1;}
    public InputStream input(Uri uri)throws IOException{InputStream in=getContentResolver().openInputStream(uri);if(in==null)throw new IOException("SOURCE_OPEN");InputStream tracked=io.input(in);if(cancel){tracked.close();throw new IOException("CANCELLED");}return tracked;}
    public OutputStream output(Uri uri)throws IOException{return output(uri,0);}
    public OutputStream output(Uri uri,long needed)throws IOException{long budget=StorageGuard.require(this,uri,needed);OutputStream out=getContentResolver().openOutputStream(uri,"wt");if(out==null)throw new IOException("TARGET_OPEN");OutputStream tracked=io.output(StorageGuard.monitor(this,uri,out,budget));if(cancel){tracked.close();throw new IOException("CANCELLED");}return tracked;}
    public void setTree(Uri uri){tree=uri.toString();currentFolder=DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri)).toString();getSharedPreferences("app",0).edit().putString("tree",tree).apply();}
    public void list(String folder)throws IOException{
        if(tree.isEmpty())throw new IOException("NO_FOLDER");
        Uri root=Uri.parse(tree);Uri doc=folder.isEmpty()?DocumentsContract.buildDocumentUriUsingTree(root,DocumentsContract.getTreeDocumentId(root)):Uri.parse(folder);
        // Only the explicitly granted tree is accepted, never arbitrary caller-supplied content URIs.
        if(!doc.getAuthority().equals(root.getAuthority()))throw new IOException("NO_FOLDER");
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(root,DocumentsContract.getDocumentId(doc));JSONArray entries=new JSONArray();
        try(Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE},null,null,null)){
            if(c==null)throw new IOException("NO_FOLDER");
            while(c.moveToNext()){if(entries.length()>=1000){error="FOLDER_LIMIT";break;}entries.put(obj("uri",DocumentsContract.buildDocumentUriUsingTree(root,c.getString(0)).toString(),"name",c.getString(1),"directory",DocumentsContract.Document.MIME_TYPE_DIR.equals(c.getString(2)),"size",c.isNull(3)?-1:c.getLong(3)));}
        }catch(SecurityException e){throw new IOException("FOLDER_PERMISSION");}
        files=entries;currentFolder=doc.toString();
    }
    public void copyFiles(List<Uri> sources)throws Exception{
        if(tree.isEmpty())throw new IOException("NO_FOLDER");
        final String destination=currentFolder.isEmpty()?DocumentsContract.buildDocumentUriUsingTree(Uri.parse(tree),DocumentsContract.getTreeDocumentId(Uri.parse(tree))).toString():currentFolder;
        queue("copy",false,()->{
            long all=0,base=0;for(Uri u:sources){long n=size(u);if(n<0){all=-1;break;}all+=n;}
            StorageGuard.require(this,Uri.parse(destination),Math.max(0,all));
            for(Uri source:sources){
                progress(base,all);String name=displayName(source);Uri dir=Uri.parse(destination);
                // Never overwrite existing files or a source on the same tree.
                ensureNameAvailable(dir,name);
                Uri target=DocumentsContract.createDocument(getContentResolver(),dir,"application/octet-stream",name);
                if(target==null)throw new IOException("TARGET_OPEN");boolean complete=false;
                try{try(InputStream in=input(source);OutputStream out=output(target)){
                    byte[] bytes=new byte[65536];int n;while((n=in.read(bytes))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");progress(base,all);out.write(bytes,0,n);base+=n;}out.flush();}complete=true;
                }finally{if(!complete){try{DocumentsContract.deleteDocument(getContentResolver(),target);}catch(Exception ignored){}}}
            }
            progress(base,all);list(destination);return "COPIED "+sources.size();
        });
    }
    private String outputFolderName(Uri destination,String source)throws IOException{List<String> names=new ArrayList<>();Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(Uri.parse(tree),DocumentsContract.getDocumentId(destination));try(Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},null,null,null)){if(c==null)throw new IOException("NO_FOLDER");while(c.moveToNext()){if(names.size()>=10000)throw new IOException("FOLDER_LIMIT");names.add(c.getString(0));}}return OutputNames.unique(OutputNames.stem(source),names);}
    public void addZipInputs(List<Uri> sources)throws IOException{if(running)throw new IOException("JOB_BUSY");synchronized(zipInputs){for(Uri u:sources)if(!zipInputs.contains(u)){if(zipInputs.size()>=1000)throw new IOException("ZIP_LIMIT");zipInputs.add(u);zipInputNames.put(u,displayName(u));}}}
    public void removeZipInput(int index)throws IOException{if(running)throw new IOException("JOB_BUSY");synchronized(zipInputs){if(index<0||index>=zipInputs.size())throw new IOException("NO_FILE");Uri removed=zipInputs.remove(index);zipInputNames.remove(removed);}}
    public void createSelectedZip()throws Exception{List<Uri> sources;synchronized(zipInputs){sources=new ArrayList<>(zipInputs);}if(sources.isEmpty())throw new IOException("ZIP_EMPTY");createZip(sources);}
    public void ensureNameAvailable(Uri dir,String name)throws IOException{
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(Uri.parse(tree),DocumentsContract.getDocumentId(dir));
        try(Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},null,null,null)){
            if(c==null)throw new IOException("NO_FOLDER");while(c.moveToNext())if(name.equalsIgnoreCase(c.getString(0)))throw new IOException("FILE_EXISTS "+name);
        }
    }

    public void clearHistory()throws IOException{
        if(running){throw new IOException("JOB_BUSY");}
        history=new JSONArray();result=error="";status="idle";getSharedPreferences("app",0).edit().remove("history").apply();
    }
    public void forgetFolder()throws IOException{
        if(running){throw new IOException("JOB_BUSY");}
        for(android.content.UriPermission permission:getContentResolver().getPersistedUriPermissions()){
            Uri u=permission.getUri();try{getContentResolver().releasePersistableUriPermission(u,(permission.isReadPermission()?Intent.FLAG_GRANT_READ_URI_PERMISSION:0)|(permission.isWritePermission()?Intent.FLAG_GRANT_WRITE_URI_PERMISSION:0));}catch(SecurityException ignored){}
        }
        tree=currentFolder=image=imageName="";imageSize=-1;files=new JSONArray();getSharedPreferences("app",0).edit().remove("tree").apply();
    }
    public void extractZip()throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(tree.isEmpty())throw new IOException("NO_FOLDER");
        Uri source=Uri.parse(image);Uri destination=Uri.parse(currentFolder.isEmpty()?DocumentsContract.buildDocumentUriUsingTree(Uri.parse(tree),DocumentsContract.getTreeDocumentId(Uri.parse(tree))).toString():currentFolder);
        queue("extract",false,()->{
            StorageGuard.require(this,destination,0);
            String finalName=outputFolderName(destination,displayName(source));String stamp=Long.toString(System.currentTimeMillis());ensureNameAvailable(destination,".extracting-"+stamp);Uri root=DocumentsContract.createDocument(getContentResolver(),destination,DocumentsContract.Document.MIME_TYPE_DIR,".extracting-"+stamp);if(root==null)throw new IOException("TARGET_OPEN");boolean complete=false;
            try{
                Map<String,Uri> dirs=new HashMap<>();dirs.put("",root);
                ZipTools.Sink sink=new ZipTools.Sink(){
                    public void directory(String path)throws IOException{
                        String current="";for(String part:path.split("/")){String next=current.isEmpty()?part:current+"/"+part;if(!dirs.containsKey(next)){try{Uri created=DocumentsContract.createDocument(getContentResolver(),dirs.get(current),DocumentsContract.Document.MIME_TYPE_DIR,part);if(created==null)throw new IOException("TARGET_OPEN");dirs.put(next,created);}catch(java.io.FileNotFoundException e){throw new IOException("TARGET_OPEN",e);}}current=next;}
                    }
                    public OutputStream file(String path)throws IOException{
                        int slash=path.lastIndexOf('/');String parent=slash<0?"":path.substring(0,slash),name=slash<0?path:path.substring(slash+1);Uri dir=dirs.get(parent);ensureNameAvailable(dir,name);Uri file=DocumentsContract.createDocument(getContentResolver(),dir,"application/octet-stream",name);if(file==null)throw new IOException("TARGET_OPEN");return output(file);
                    }
                };
                long expanded=ZipTools.extract(input(source),sink,2L*1024*1024*1024,10000,this::progress);
                if(DocumentsContract.renameDocument(getContentResolver(),root,finalName)==null)throw new IOException("RENAME_FAILED");complete=true;return "EXTRACTED_NAMED\n"+displayName(source)+"\n"+finalName+"\n"+expanded;
            }finally{if(!complete)try{DocumentsContract.deleteDocument(getContentResolver(),root);}catch(Exception ignored){}}
        });
    }
    public void importVentoy()throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");Uri source=Uri.parse(image);
        queue("ventoyImport",false,()->{
            if(getFilesDir().getUsableSpace()<VentoyBundle.EFI_BYTES+VentoyBundle.CORE_BYTES+StorageGuard.RESERVE)throw new IOException("SPACE_LOW");
            File stage=new File(getFilesDir(),"ventoy-import-"+System.currentTimeMillis()),current=new File(getFilesDir(),"ventoy-bundle"),old=new File(getFilesDir(),"ventoy-old");
            VentoyBundle bundle=VentoyBundle.importPackage(input(source),stage,this::progress);VentoyBundle.removeOwned(old);
            if(current.exists()&&!current.renameTo(old)){VentoyBundle.removeOwned(stage);throw new IOException("VENTOY_CACHE");}
            if(!stage.renameTo(current)){if(old.exists())old.renameTo(current);VentoyBundle.removeOwned(stage);throw new IOException("VENTOY_CACHE");}VentoyBundle.removeOwned(old);
            getSharedPreferences("app",0).edit().putString("ventoyVersion",bundle.version).apply();return "VENTOY_IMPORTED "+bundle.version;
        });
    }
    private void syncDocument(Uri file)throws IOException{try(ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(file,"rw")){if(fd==null)throw new IOException("TARGET_OPEN");fd.getFileDescriptor().sync();}}
    public void consoleSplit(String kind,int partMiB,String outputName)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(partMiB<1||partMiB>3840)throw new IOException("CONSOLE_OPTIONS");ConsoleParts.name(outputName,kind);Uri source=Uri.parse(image),dest=destination();
        queue("consoleSplit",false,()->{try(OpticalImage.Source input=SafImages.open(this,source,io)){if(input.size()<1||input.size()>ConsoleParts.MAX_BYTES)throw new IOException("CONSOLE_OPTIONS");StorageGuard.require(this,dest,input.size()+8192);String name="Console-parts-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{ConsoleParts.Manifest m=ConsoleParts.split(input,outputName,kind,partMiB*1048576L,new ConsoleParts.Sink(){public OutputStream create(String path)throws IOException{Uri file=sink.create(path);if(!displayName(file).equals(path))throw new IOException("CONSOLE_PROVIDER");return new BufferedOutputStream(output(file),65536);}public InputStream reopen(String path)throws IOException{return sink.reopen(path);}public void sync(String path)throws IOException{syncDocument(sink.files.get(path));}},this::progress);ok=true;return "CONSOLE_SPLIT_OK\n"+m.pieces.size()+"\n"+m.manifestName()+"\n"+m.hash();}
            finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public void consoleJoin(List<Uri> supplied)throws Exception{
        if(supplied.size()<2||supplied.size()>65)throw new IOException("CONSOLE_PARTS");Uri dest=destination();
        queue("consoleJoin",false,()->{Uri manifestUri=null;Map<String,ConsoleParts.Part> parts=new HashMap<>();for(Uri source:supplied){String name=displayName(source);if(name.endsWith(".usbwb")){if(manifestUri!=null)throw new IOException("CONSOLE_MANIFEST");manifestUri=source;}else{if(parts.containsKey(name))throw new IOException("CONSOLE_PARTS");long bytes=size(source);if(bytes<1)throw new IOException("CONSOLE_PART_SIZE");parts.put(name,new ConsoleParts.Part(){public long size(){return bytes;}public InputStream input()throws IOException{return WorkbenchApp.this.input(source);}});}}
            if(manifestUri==null)throw new IOException("CONSOLE_MANIFEST");ConsoleParts.Manifest manifest;try(InputStream in=input(manifestUri)){manifest=ConsoleParts.read(in);}if(!displayName(manifestUri).equals(manifest.manifestName()))throw new IOException("CONSOLE_MANIFEST");phase="parts_check";ConsoleParts.join(manifest,parts,null,this::progress);StorageGuard.require(this,dest,manifest.bytes);String name="Console-joined-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{phase="parts_join";Uri file=sink.create(manifest.name);try(OutputStream out=new BufferedOutputStream(output(file,manifest.bytes),65536)){ConsoleParts.join(manifest,parts,out,this::progress);}syncDocument(file);phase="parts_verify";try(InputStream in=input(file)){ConsoleParts.verifyJoined(manifest,in,this::progress);}ok=true;return "CONSOLE_JOIN_OK\n"+manifest.name+"\n"+manifest.hash();}
            finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        });
    }
    public void fatImage(boolean extract,int partition)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(partition<0||partition>128)throw new IOException("FAT_IMAGE_PARTITION");Uri source=Uri.parse(image),dest=extract?destination():null;
        queue(extract?"fatExtract":"fatList",false,()->{try(OpticalImage.Source input=SafImages.open(this,source,io)){OpticalImage.Manifest manifest=FatImage.scan(input,partition,this::progress);
            if(!extract){StringBuilder text=new StringBuilder("FAT_IMAGE_LIST "+manifest.filesystem+"\n");for(int i=0;i<Math.min(200,manifest.entries.size());i++){OpticalImage.Entry e=manifest.entries.get(i);text.append(e.path).append(e.directory?"/":"").append(" · ").append(e.size).append('\n');}return text.toString();}
            StorageGuard.require(this,dest,manifest.totalBytes);String name="FAT-image-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;try{OpticalImage.extract(input,manifest,sink,this::progress);ok=true;return "FAT_IMAGE_EXTRACTED";}finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public void convertImage(String mode,double limitGiB)throws Exception{
        if(!ImageContainers.supported(mode))throw new IOException("JOB_UNKNOWN");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("CONVERT_LIMIT");if(image.isEmpty())throw new IOException("NO_IMAGE");Uri source=Uri.parse(image),dest=destination();
        queue(mode,false,()->{phase="convert_prepare";long max=limitBytes(limitGiB);try(OpticalImage.Source input=SafImages.open(this,source,io)){
            long budget=ImageContainers.budget(mode,input,max,this::progress);StorageGuard.require(this,dest,budget);String name="Converted-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{String leaf=mode.equals("isoZso")?"image.zso":mode.equals("isoCso")?"image.cso":(mode.equals("csoIso")||mode.equals("zsoIso"))?"image.iso":mode.equals("gzipPack")?"image.img.gz":"image.img";Uri file=sink.create(leaf);ImageContainers.Result result;phase="convert_write";
                try(OutputStream out=new BufferedOutputStream(output(file,budget),65536)){result=ImageContainers.convert(mode,input,out,max,this::progress);}if((mode.equals("gzipUnpack")||mode.equals("xzUnpack")||mode.equals("dmgRaw"))&&result.bytes!=budget)throw new IOException("SOURCE_CHANGED");
                try(ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(file,"rw")){if(fd==null)throw new IOException("TARGET_OPEN");fd.getFileDescriptor().sync();}
                phase="convert_verify";try(OpticalImage.Source saved=SafImages.open(this,file,io)){ImageContainers.verify(mode,saved,result,this::progress);}ok=true;return "CONTAINER_OK "+leaf+"\n"+result.bytes+"\nSHA-256: "+result.hash();
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public void fileBenchmark(int size)throws Exception{
        Uri dest=destination();if(size<1||size>1024)throw new IOException("BENCH_SIZE");
        queue("fileBenchmark",false,()->{StorageGuard.require(this,dest,size*1048576L);String name=".usbwb-benchmark-"+System.currentTimeMillis()+".tmp";ensureNameAvailable(dest,name);Uri file=DocumentsContract.createDocument(getContentResolver(),dest,"application/octet-stream",name);if(file==null)throw new IOException("TARGET_OPEN");
            try{return FileBenchmark.run(new FileBenchmark.File(){public OutputStream output()throws IOException{return WorkbenchApp.this.output(file);}public InputStream input()throws IOException{return WorkbenchApp.this.input(file);}public void sync()throws IOException{try(ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(file,"rw")){if(fd==null)throw new IOException("TARGET_OPEN");fd.getFileDescriptor().sync();}}},size,this::progress);}
            finally{if(!DocumentsContract.deleteDocument(getContentResolver(),file))throw new IOException("BENCH_CLEANUP "+name);}
        });
    }
    public void persistence(int mebibytes,String label,String imagePath)throws Exception{
        PersistenceImage.size(mebibytes,label);
        String path=imagePath.startsWith("/")?imagePath.substring(1):imagePath;try{ZipTools.safePath(path);}catch(IOException e){throw new IOException("PERSISTENCE_ISO_PATH");}if(!path.toLowerCase(Locale.ROOT).endsWith(".iso"))throw new IOException("PERSISTENCE_ISO_PATH");Uri dest=destination();
        queue("persistence",false,()->{long bytes=mebibytes*1048576L;StorageGuard.require(this,dest,bytes+1048576);String name="Persistence-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{Uri file=sink.create("persistence.dat");try(DocumentDisk disk=new DocumentDisk(this,file,bytes,io)){PersistenceImage.create(this,disk,label,label.equals("persistence"),this::progress,()->cancel);}
                JSONObject item=obj("image","/"+path,"backend","/persistence.dat");JSONObject settings=obj("persistence",new JSONArray().put(item));try(OutputStream out=sink.file("ventoy-persistence-template.json")){out.write(settings.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));}ok=true;return "PERSISTENCE_CREATED "+label;
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        });
    }
    private static long limitBytes(double gib)throws IOException{if(Double.isNaN(gib)||Double.isInfinite(gib)||gib<1.0/1024||gib>16)throw new IOException("CONVERT_LIMIT");return (long)Math.floor(gib*1073741824L);}
    public void createChd(int unit,double limitGiB)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(unit!=512&&unit!=2048)throw new IOException("CHD_CREATE_OPTIONS");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("CONVERT_LIMIT");Uri source=Uri.parse(image),dest=destination();long limit=limitBytes(limitGiB);
        queue("chdCreate",false,()->{try(OpticalImage.Source input=SafImages.open(this,source,io)){long size=input.size(),bytes=ChdStored.outputSize(size,unit,limit);StorageGuard.require(this,dest,bytes+16L*1048576);String name="CHD-created-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{phase="chd_create";String hash;try(WimFiles.RandomFile output=SafImages.wimStore(this,sink,io,"","image.chd").create("export.bin")){hash=ChdStored.create(input,output,unit,limit,this::progress,()->cancel);}phase="chd_verify";try(OpticalImage.Source saved=SafImages.open(this,sink.files.get("image.chd"),io)){ChdStored.verify(new File(getApplicationInfo().nativeLibraryDir),saved,hash,limit,this::progress,()->cancel);}ok=true;return "CHD_STORED_OK "+size+"\nSHA-256: "+hash;
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public void chdTool(double limitGiB)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("CONVERT_LIMIT");Uri source=Uri.parse(image),dest=destination();
        queue("chdRaw",false,()->{try(OpticalImage.Source input=SafImages.open(this,source,io)){File libs=new File(getApplicationInfo().nativeLibraryDir);phase="chd_check";ChdTools.Plan plan=ChdTools.inspect(libs,input,limitBytes(limitGiB),this::progress,()->cancel);StorageGuard.require(this,dest,plan.bytes+1048576);String name="CHD-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{phase="chd_convert";String hash=ChdTools.convert(libs,input,plan,SafImages.wimStore(this,sink,io,"","image.img"),this::progress,()->cancel);phase="chd_verify";try(InputStream in=sink.reopen("image.img")){ChdTools.verify(in,plan,hash,this::progress);}ok=true;return "CHD_RAW_OK "+plan.bytes+"\nSHA-256: "+hash;
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public void fsImage(boolean extract,int partition,double limitGiB)throws Exception{if(partition<0||partition>128)throw new IOException("FS_PARTITION");imageBrowse(extract,limitGiB,partition);}
    public void archiveTool(boolean extract,double limitGiB)throws Exception{archivePassword(extract,limitGiB,"");}
    public void archivePassword(boolean extract,double limitGiB,String password)throws Exception{
        if(password==null||password.length()>256||password.indexOf('\0')>=0)throw new IOException("ARCHIVE_PASSWORD");if(image.isEmpty())throw new IOException("NO_IMAGE");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("ARCHIVE_LIMIT");Uri source=Uri.parse(image),dest=extract?destination():null;
        queue(extract?"archiveExtract":"archiveList",false,()->{try(OpticalImage.Source input=SafImages.open(this,source,io)){return browseEngine(input,null,extract,limitGiB,dest,false,null,password);}});
    }
    private void imageBrowse(boolean extract,double limitGiB,Integer partition)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("ARCHIVE_LIMIT");Uri source=Uri.parse(image),dest=extract?destination():null;
        queue(partition==null?(extract?"archiveExtract":"archiveList"):(extract?"fsimageExtract":"fsimageList"),false,()->{try(OpticalImage.Source owner=SafImages.open(this,source,io)){return browseOpened(owner,partition,extract,limitGiB,dest,false);}});
    }
    public void fsUsb(boolean extract,int partition,double limitGiB,String id,long epoch)throws Exception{
        if(partition<0||partition>128)throw new IOException("FS_PARTITION");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("ARCHIVE_LIMIT");Uri dest=extract?destination():null;
        if(extract&&(!"com.android.externalstorage.documents".equals(dest.getAuthority())||!DocumentsContract.getDocumentId(dest).startsWith("primary:")))throw new IOException("FSUSB_INTERNAL");
        queue(extract?"fsusbExtract":"fsusbList",true,()->{if(!id.equals(selected)||epoch!=deviceEpoch)throw new IOException("DEVICE_CHANGED");try(UsbDisk disk=new UsbDisk(usb(),target(),true)){UsbFileSource view=new UsbFileSource(disk);return browseOpened(view,partition,extract,limitGiB,dest,true);}});
    }
    private String browseOpened(OpticalImage.Source owner,Integer partition,boolean extract,double limitGiB,Uri dest,boolean live)throws Exception{return browseEngine(owner,partition,extract,limitGiB,dest,live,null);}
    private String browseEngine(OpticalImage.Source owner,Integer partition,boolean extract,double limitGiB,Uri dest,boolean live,String wim)throws Exception{return browseEngine(owner,partition,extract,limitGiB,dest,live,wim,"");}
    private String browseEngine(OpticalImage.Source owner,Integer partition,boolean extract,double limitGiB,Uri dest,boolean live,String wim,String password)throws Exception{
        OpticalImage.Source input=owner;String kind=null;if(partition!=null){FsImageTools.View view=FsImageTools.open(owner,partition);input=view.source;kind=view.kind;}File libs=new File(getApplicationInfo().nativeLibraryDir);phase=partition==null?"archive_scan":"fsimage_scan";ArchiveTools.Manifest manifest=wim!=null?ArchiveTools.scanWim(libs,input,Integer.parseInt(wim.substring(0,wim.indexOf(':'))),wim.substring(wim.indexOf(':')+1),limitBytes(limitGiB),this::progress,()->cancel):kind==null?ArchiveTools.scanPassword(libs,input,limitBytes(limitGiB),this::progress,()->cancel,password):ArchiveTools.scanFilesystem(libs,input,limitBytes(limitGiB),this::progress,()->cancel,kind,extract&&!live);if(!extract)return ArchiveTools.listing(manifest);
            StorageGuard.require(this,dest,manifest.total+manifest.entries.size()*4096L+16L*1048576);String name=outputFolderName(dest,live?"USB files":imageName);ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
            try{Map<String,ArchiveTools.Entry> paths=new HashMap<>();for(ArchiveTools.Entry e:manifest.entries){if(e.directory){StorageGuard.require(this,root,65536);sink.directory(e.path);}else if(!e.special)paths.put(e.output(),e);}if(live&&owner instanceof UsbFileSource)((UsbFileSource)owner).invalidate();phase=partition==null?"archive_extract":"fsimage_extract";Map<String,String> hashes=ArchiveTools.extractPassword(libs,input,manifest,new WimFiles.Store(){public void capacity(long n)throws IOException{progress(0,-1);StorageGuard.require(WorkbenchApp.this,root,n+65536);}public WimFiles.RandomFile create(String n)throws IOException{ArchiveTools.Entry e=paths.get(n);if(e==null)throw new IOException("ARCHIVE_PATH");return SafImages.wimStore(WorkbenchApp.this,sink,io,"",e.path).create("export.bin");}},this::progress,()->cancel,password);
                phase=partition==null?"archive_verify":"fsimage_verify";for(ArchiveTools.Entry e:manifest.entries)if(!e.directory&&!e.special)try(InputStream in=sink.reopen(e.path)){if(!DiskTools.sha256(in,e.size,this::progress).equals(hashes.get(e.output())))throw new IOException("VERIFY_FAILED");}int skipped=0;for(ArchiveTools.Entry e:manifest.entries)if(e.special)skipped++;ok=true;return "OUTPUT_NAMED\n"+(live?"USB files":imageName)+"\n"+name+"\n"+(wim!=null?"WIMTREE_ALL_OK ":partition==null?"ARCHIVE_ALL_OK ":"FSIMAGE_ALL_OK ")+paths.size()+"\n"+manifest.total+"\n"+skipped;
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
    }
    private android.util.AtomicFile readMapFile(){return new android.util.AtomicFile(new File(getFilesDir(),"drive-read-map.bin"));}
    private ReadMap.State loadReadMap()throws IOException{try(InputStream in=readMapFile().openRead()){return ReadMap.decode(in);}catch(FileNotFoundException e){throw new IOException("READ_MAP_MISSING");}}
    private void saveReadMap(ReadMap.State map)throws IOException{byte[] expected=map.encode();android.util.AtomicFile file=readMapFile();FileOutputStream out=null;try{out=file.startWrite();out.write(expected);file.finishWrite(out);try(InputStream saved=file.openRead()){if(!Arrays.equals(expected,ReadMap.decode(saved).encode()))throw new IOException("READ_MAP_SAVE");}}catch(Exception e){if(out!=null)file.failWrite(out);throw new IOException("READ_MAP_SAVE",e);}}

    public void readMap(String mode,String id,long epoch)throws Exception{
        if(mode.equals("show")){queue("readMapShow",false,()->ReadMap.report(loadReadMap()));return;}
        if(!mode.equals("new")&&!mode.equals("resume"))throw new IOException("READ_MAP_OPTIONS");
        queue("readMap",true,()->{if(!id.equals(selected)||epoch!=deviceEpoch)throw new IOException("DEVICE_CHANGED");UsbDevice device=target();String serial="";try{serial=device.getSerialNumber();}catch(SecurityException ignored){}String key=device.getVendorId()+":"+device.getProductId()+":"+serial;
            try(UsbDisk disk=new UsbDisk(usb(),device,true)){byte[] identity=ReadMap.identity(disk,key);ReadMap.State map=mode.equals("new")?ReadMap.fresh(disk,identity):loadReadMap();phase="read_map_scan";ReadMap.scan(disk,map,identity,(done,total)->progress(done*512,total*512),()->cancel,this::saveReadMap);return ReadMap.report(map);}
        });
    }
    public void wimTree(boolean extract,int index,double limitGiB,String path)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(index<1||index>256)throw new IOException("WIM_INDEX");if(Double.isNaN(limitGiB)||Double.isInfinite(limitGiB)||limitGiB<1.0/1024||limitGiB>16)throw new IOException("ARCHIVE_LIMIT");Uri source=Uri.parse(image),dest=extract?destination():null;
        queue(extract?"wimTreeExtract":"wimTreeList",false,()->{try(OpticalImage.Source owner=SafImages.open(this,source,io)){return browseEngine(owner,null,extract,limitGiB,dest,false,index+":"+path);}});
    }
    public void usbExtreme(boolean inspect,String title,String media)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");Uri source=Uri.parse(image),dest=inspect?null:destination();
        queue(inspect?"ulInspect":"ulCreate",false,()->{
            if(inspect){try(InputStream in=input(source)){List<UsbExtreme.Game> games=UsbExtreme.read(in);StringBuilder report=new StringBuilder("UL_LIST\n");for(UsbExtreme.Game g:games)report.append(g.title).append(" • ").append(g.id).append(" • ").append(g.media==0x14?"DVD":"CD").append(" • ").append(g.parts).append('\n');return report.toString();}}
            try(OpticalImage.Source owner=SafImages.open(this,source,io)){long bytes=owner.size();if(bytes<2048||bytes>UsbExtreme.MAX)throw new IOException("UL_OPTIONS");UsbExtreme.game(title,UsbExtreme.discId(owner),(int)((bytes+UsbExtreme.PART-1)/UsbExtreme.PART),media.equals("DVD")?0x14:media.equals("CD")?0x12:0);StorageGuard.require(this,dest,bytes+16L*1048576);String name="USBExtreme-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
                try{String report=UsbExtreme.write(owner,title,media,new ConsoleParts.Sink(){public OutputStream create(String path)throws IOException{StorageGuard.require(WorkbenchApp.this,root,65536);Uri file=sink.create(path);if(!displayName(file).equals(path))throw new IOException("CONSOLE_PROVIDER");return new BufferedOutputStream(output(file),65536);}public InputStream reopen(String path)throws IOException{return sink.reopen(path);}public void sync(String path)throws IOException{syncDocument(sink.files.get(path));}},this::progress);ok=true;return "UL_CREATED\n"+report;}finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}}
        });
    }
    public void wimTool(boolean export,int index,String wanted)throws Exception{
        if(image.isEmpty())throw new IOException("NO_IMAGE");if(index<1||index>256)throw new IOException("WIM_INDEX");String path=export?WimPaths.normalize(wanted,false):"/";if(export){try{ZipTools.safePath(path.substring(1));}catch(IOException e){throw new IOException("WIM_FILE_PATH");}if(path.endsWith("/"))throw new IOException("WIM_FILE_PATH");}Uri source=Uri.parse(image),dest=export?destination():null;
        queue(export?"wimExport":"wimList",false,()->{File libs=new File(getApplicationInfo().nativeLibraryDir);if(!WimProcess.available(libs))throw new IOException("ENGINE_MISSING");try(OpticalImage.Source optical=SafImages.open(this,source,io)){
            if(!export)try(WimFiles probe=new WimFiles(SafImages.slice(optical,new OpticalImage.Entry("source.wim",false,optical.size(),Arrays.asList(new OpticalImage.Range(0,optical.size())))),rejectWimOutput())){return "WIM_LIST\n"+WimProcess.run(libs,Arrays.asList("dir","/usbwb/source.wim",Integer.toString(index)),probe,this::progress,()->cancel);}
            StorageGuard.require(this,dest,0);String name="WIM-export-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;String leaf=path.substring(path.lastIndexOf('/')+1);
            try(WimFiles files=new WimFiles(SafImages.slice(optical,new OpticalImage.Entry("source.wim",false,optical.size(),Arrays.asList(new OpticalImage.Range(0,optical.size())))),SafImages.wimStore(this,sink,io,"",leaf))){
                WimProcess.run(libs,Arrays.asList("extract","/usbwb/source.wim",Integer.toString(index),path,"--to-stdout","--no-globs","--quiet"),files,this::progress,()->cancel,true);
                if(files.names().isEmpty()){FileProtocol.Handle empty=files.open("export.bin",578);files.close(empty.id);}files.seal();String hash;try(InputStream in=sink.reopen(leaf)){hash=DiskTools.sha256(in,files.pathSize("export.bin"),this::progress);}ok=true;return "WIM_EXPORTED "+leaf+"\nSHA-256: "+hash;
            }finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
        }});
    }
    public VentoyBundle ventoyBundle()throws IOException{File directory=new File(getFilesDir(),"ventoy-bundle");if(!new File(directory,"bundle.properties").isFile())throw new IOException("VENTOY_PACKAGE");return VentoyBundle.load(directory);}
    public void imageTool(String mode)throws Exception{imageTool(mode,3800);}
    public void imageTool(String mode,int wimPartMiB)throws Exception{
        if(wimPartMiB<1||wimPartMiB>3800)throw new IOException("WIM_PART_SIZE");
        if(!Arrays.asList("opticalList","opticalExtract","tarList","tarExtract","windows","wimSplit").contains(mode))throw new IOException("JOB_UNKNOWN");
        if(image.isEmpty())throw new IOException("NO_IMAGE");Uri source=Uri.parse(image);Uri dest=mode.equals("opticalList")||mode.equals("tarList")?null:destination();
        queue(mode,false,()->{
            if(mode.startsWith("tar")){
                if(mode.equals("tarList")){StringBuilder text=new StringBuilder("TAR / GZIP / XZ\n");int[] shown={0};int count=TarTools.scan(input(source),(entry,in)->{if(shown[0]++<200)text.append(entry.path).append(entry.directory()?"/":"").append(" · ").append(entry.size).append('\n');},2L*1024*1024*1024,10000,this::progress);return text+"\n"+tr("Entries: ","العناصر: ")+count;}
                StorageGuard.require(this,dest,0);String name=outputFolderName(dest,displayName(source));ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
                try{long n=TarTools.extract(input(source),sink,2L*1024*1024*1024,10000,this::progress);ok=true;return "EXTRACTED_NAMED\n"+displayName(source)+"\n"+name+"\n"+n;}finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
            }
            try(OpticalImage.Source optical=SafImages.open(this,source,io)){
                if(mode.equals("wimSplit")){
                    File libs=new File(getApplicationInfo().nativeLibraryDir);if(!WimProcess.available(libs))throw new IOException("ENGINE_MISSING");
                    try(WimFiles probe=new WimFiles(SafImages.slice(optical,new OpticalImage.Entry("source.wim",false,optical.size(),Arrays.asList(new OpticalImage.Range(0,optical.size())))),rejectWimOutput())){WimProcess.run(libs,Arrays.asList("verify","/usbwb/source.wim","--quiet"),probe,this::progress,()->cancel);}
                    StorageGuard.require(this,dest,optical.size()+16*1024*1024);String name="WIM-parts-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
                    try(WimFiles parts=new WimFiles(SafImages.slice(optical,new OpticalImage.Entry("source.wim",false,optical.size(),Arrays.asList(new OpticalImage.Range(0,optical.size())))),SafImages.wimStore(this,sink,io,""))){WimProcess.split(libs,parts,this::progress,()->cancel,wimPartMiB);ok=true;return "WIM_SPLIT_OK "+parts.names().size();}finally{if(!ok){sink.rollback();if(SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
                }
                OpticalImage.Manifest manifest=OpticalImage.scan(optical,this::progress);
                if(mode.equals("opticalList")){StringBuilder text=new StringBuilder(manifest.filesystem+"\n");int count=0;for(OpticalImage.Entry e:manifest.entries)if(count++<200)text.append(e.path).append(e.directory?"/":"").append(" · ").append(e.size).append('\n');return text+"\n"+tr("Entries: ","العناصر: ")+manifest.entries.size()+" · "+manifest.totalBytes+" bytes / بايت";}
                boolean windows=mode.equals("windows");OpticalImage.Entry wim=manifest.find("sources/install.wim");boolean split=windows&&wim!=null&&wim.size>FileProtocol.MAX_FILE;String arch="";
                if(windows){SafImages.removableRoot(dest);if(!SafImages.empty(this,dest))throw new IOException("WINDOWS_EMPTY_ROOT");arch=manifest.windowsPreflight(optical,true);if(split)for(OpticalImage.Entry e:manifest.entries)if(e.path.toLowerCase(Locale.ROOT).matches("sources/install([0-9]+)?\\.swm"))throw new IOException("WINDOWS_IMAGE_SET");}
                File libs=new File(getApplicationInfo().nativeLibraryDir);
                if(split){if(!WimProcess.available(libs))throw new IOException("ENGINE_MISSING");try(WimFiles probe=new WimFiles(SafImages.slice(optical,wim),rejectWimOutput())){WimProcess.run(libs,Arrays.asList("verify","/usbwb/source.wim","--quiet"),probe,this::progress,()->cancel);}}
                StorageGuard.require(this,dest,manifest.totalBytes+(split?16*1024*1024:0));Uri root=dest;
                if(!windows){String name="Image-"+System.currentTimeMillis();ensureNameAvailable(dest,name);root=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(root==null)throw new IOException("TARGET_OPEN");}
                SafImages.Tree sink=new SafImages.Tree(this,root);boolean ok=false;
                try{OpticalImage.Manifest copy=manifest;if(split){copy=new OpticalImage.Manifest(manifest.filesystem);for(OpticalImage.Entry e:manifest.entries)if(e!=wim)copy.add(e);}OpticalImage.extract(optical,copy,sink,this::progress);
                    if(split)try(WimFiles parts=new WimFiles(SafImages.slice(optical,wim),SafImages.wimStore(this,sink,io,wim.path.substring(0,wim.path.lastIndexOf('/'))))){WimProcess.split(libs,parts,this::progress,()->cancel,wimPartMiB);}
                    ok=true;return windows?"WINDOWS_UEFI_OK "+arch:"OPTICAL_EXTRACTED_OK";
                }finally{if(!ok){sink.rollback();if(!windows&&SafImages.empty(this,root))DocumentsContract.deleteDocument(getContentResolver(),root);}}
            }
        });
    }
    private static WimFiles.Store rejectWimOutput(){return new WimFiles.Store(){public WimFiles.RandomFile create(String name)throws IOException{throw new IOException("READ_ONLY");}public void capacity(long bytes)throws IOException{throw new IOException("READ_ONLY");}};}
    private Uri destination()throws IOException{if(tree.isEmpty())throw new IOException("NO_FOLDER");return Uri.parse(currentFolder.isEmpty()?DocumentsContract.buildDocumentUriUsingTree(Uri.parse(tree),DocumentsContract.getTreeDocumentId(Uri.parse(tree))).toString():currentFolder);}
    public void splitFile(int partSize)throws Exception{
        if(image.isEmpty()||imageSize<1)throw new IOException("NO_IMAGE");Uri source=Uri.parse(image),dest=destination();long size=imageSize;
        queue("split",false,()->{if(partSize<1048576||partSize>2146435072||size/partSize>99998)throw new IOException("SPLIT_SIZE");long parts=(size+partSize-1)/partSize;if(parts>99999)throw new IOException("SPLIT_SIZE");StorageGuard.require(this,dest,size+parts*56);
            String name="Parts-"+System.currentTimeMillis();ensureNameAvailable(dest,name);Uri folder=DocumentsContract.createDocument(getContentResolver(),dest,DocumentsContract.Document.MIME_TYPE_DIR,name);if(folder==null)throw new IOException("TARGET_OPEN");boolean ok=false;
            try{WorkshopTools.split(input(source),size,partSize,index->{String part=String.format(Locale.ROOT,"part-%05d.uwp",index);Uri u=DocumentsContract.createDocument(getContentResolver(),folder,"application/octet-stream",part);if(u==null)throw new IOException("TARGET_OPEN");return output(u);},this::progress);ok=true;return "SPLIT_OK";}finally{if(!ok)try{DocumentsContract.deleteDocument(getContentResolver(),folder);}catch(Exception ignored){}}
        });
    }
    public void joinFiles(List<Uri> sources)throws Exception{
        Uri dest=destination();queue("join",false,()->{List<WorkshopTools.Part> parts=new ArrayList<>();for(Uri u:sources){progress(parts.size(),-1);parts.add(new WorkshopTools.Part(()->input(u)));}if(parts.isEmpty())throw new IOException("PART_MISSING");long size=parts.get(0).size;StorageGuard.require(this,dest,size);
            String name="Joined-"+System.currentTimeMillis()+".img";ensureNameAvailable(dest,name);Uri target=DocumentsContract.createDocument(getContentResolver(),dest,"application/octet-stream",name);if(target==null)throw new IOException("TARGET_OPEN");boolean ok=false;
            try{try(OutputStream out=output(target,size)){WorkshopTools.join(parts,out,this::progress);}ok=true;return "JOIN_OK";}finally{if(!ok)try{DocumentsContract.deleteDocument(getContentResolver(),target);}catch(Exception ignored){}}
        });
    }
    public void createZip(List<Uri> sources)throws Exception{
        Uri dest=destination();queue("createzip",false,()->{long all=0;Set<String> names=new HashSet<>();for(Uri u:sources){String n=displayName(u);if(n.isEmpty()||n.contains("/")||n.contains("\\")||n.equals(".")||n.equals("..")||!names.add(n.toLowerCase(Locale.ROOT)))throw new IOException("ZIP_PATH");long size=size(u);if(size<0||all>2147483648L-size)throw new IOException("ZIP_LIMIT");all+=size;}if(sources.size()>1000)throw new IOException("ZIP_LIMIT");StorageGuard.require(this,dest,all+all/100+sources.size()*1024L+65536);
            String name="Archive-"+System.currentTimeMillis()+".zip";ensureNameAvailable(dest,name);Uri target=DocumentsContract.createDocument(getContentResolver(),dest,"application/zip",name);if(target==null)throw new IOException("TARGET_OPEN");boolean ok=false;long done=0;
            try{try(java.util.zip.ZipOutputStream out=new java.util.zip.ZipOutputStream(output(target))){byte[] buffer=new byte[65536];for(Uri u:sources){out.putNextEntry(new java.util.zip.ZipEntry(displayName(u)));try(InputStream in=input(u)){int n;while((n=in.read(buffer))!=-1){if(n==0)throw new IOException("SOURCE_STALLED");progress(done,all);done+=n;if(done>2147483648L)throw new IOException("ZIP_LIMIT");out.write(buffer,0,n);}}out.closeEntry();}}ok=true;progress(done,all);return "ZIP_CREATED";}finally{if(!ok)try{DocumentsContract.deleteDocument(getContentResolver(),target);}catch(Exception ignored){}}
        });
    }
    public String exportReport(){StringBuilder text=new StringBuilder(tr("USB Workbench report\n","تقرير ورشة USB\n"));text.append(ReportText.render(diagnostic(),arabic())).append(tr("\n\nHistory\n","\n\nالسجل\n"));for(int i=0;i<history.length();i++){JSONObject h=history.optJSONObject(i);if(h==null)continue;text.append("\n").append(ReportText.job(h.optString("job"),arabic())).append(" · ").append(h.optString("time")).append("\n").append(ReportText.phase(h.optString("phase"),arabic())).append("\n").append(ioReport(h.optJSONObject("io"))).append("\n").append(ReportText.render(h.optString("result"),arabic())).append("\n").append(ReportText.error(h.optString("error"),arabic())).append('\n');}return text.toString();}
    public String diagnostic(){
        StringBuilder report=new StringBuilder("USB Workbench0.16.1 • Android "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\nUSB host / مضيف USB: "+getPackageManager().hasSystemFeature("android.hardware.usb.host")+"\nNative NTFS / محرك NTFS: "+NativeTools.available(this,"mkntfs")+"\nNative ext / محرك ext: "+NativeTools.available(this,"mke2fs")+"\nNative WIM / محرك WIM: "+WimProcess.available(new File(getApplicationInfo().nativeLibraryDir))+"\nFolder grant / إذن المجلد: "+!tree.isEmpty()+"\n");
        for(UsbDevice device:usb().getDeviceList().values()){
            report.append("\nUSB ").append(device.getDeviceName()).append(String.format(Locale.ROOT," VID:PID %04X:%04X",device.getVendorId(),device.getProductId())).append(" permission / إذن: ").append(usb().hasPermission(device)).append(" selected / مختار: ").append(device.getDeviceName().equals(selected)).append('\n');
            for(int i=0;i<device.getInterfaceCount();i++){UsbInterface f=device.getInterface(i);report.append("Interface / واجهة ").append(f.getId()).append(" class/subclass/protocol ").append(f.getInterfaceClass()).append('/').append(f.getInterfaceSubclass()).append('/').append(f.getInterfaceProtocol()).append('\n');for(int j=0;j<f.getEndpointCount();j++){UsbEndpoint e=f.getEndpoint(j);report.append(" endpoint / نقطة ").append(e.getAddress()).append(" type ").append(e.getType()).append(" maxPacket ").append(e.getMaxPacketSize()).append('\n');}}
        }
        for(int i=0;i<Math.min(3,history.length());i++){JSONObject past=history.optJSONObject(i);if(past!=null&&!past.optString("error").isEmpty())report.append("\nRecent app error / خطأ سابق: ").append(past.optString("job")).append(" · ").append(past.optString("error").split("\n",2)[0]).append('\n');}
        return report+"\nBOT/SCSI LUN0; no UAS/MTP; no root required.\nBOT/SCSI LUN0؛ دون UAS/MTP؛ لا يحتاج روت.\nDirect access may release Android's driver after review. Claim failure does not prove another app owns it.\nقد يفصل الوصول المباشر برنامج تشغيل أندرويد بعد المراجعة. فشل الحجز لا يثبت أن تطبيقاً آخر يمتلكه.\nFormatting support does not imply Android can mount that filesystem.\nدعم التهيئة لا يعني أن أندرويد يستطيع قراءة نظام الملفات.";
    }
    public String inspect(UsbDisk disk)throws IOException{
        UsbAnalyzer analyzer=new UsbAnalyzer(disk,this::progress,view->{
            OpticalImage.Source source=new OpticalImage.Source(){public long size(){return view.blockCount()*view.blockSize();}public void read(long at,byte[] b,int off,int n)throws IOException{int bs=view.blockSize();long first=at/bs;int skip=(int)(at%bs),length=(skip+n+bs-1)/bs*bs;byte[] data=new byte[length];view.read(first,data,length);System.arraycopy(data,skip,b,off,n);}public void close(){}};
            return ArchiveTools.statistics(new File(getApplicationInfo().nativeLibraryDir),source,(done,total)->progress(0,-1),()->cancel);
        });
        UsbDevice d=target();analyzer.field("App / Android / API","التطبيق / أندرويد / API","USB Workbench0.16.1 / "+Build.VERSION.RELEASE+" / "+Build.VERSION.SDK_INT);
        analyzer.field("USB manufacturer","مصنع USB",d.getManufacturerName());analyzer.field("USB product","منتج USB",d.getProductName());analyzer.field("VID:PID","VID:PID",String.format(Locale.ROOT,"%04X:%04X",d.getVendorId(),d.getProductId()));
        try{analyzer.field("USB serial (may be absent or spoofed)","رقم USB (قد يغيب أو يكون مزوراً)",d.getSerialNumber());}catch(SecurityException e){analyzer.field("USB serial","رقم USB","Unavailable","غير متاح");}
        analyzer.field("Device release version (bcdDevice, not USB speed)","إصدار الجهاز (bcdDevice وليس سرعة USB)",d.getVersion());analyzer.field("USB specification descriptor (not negotiated speed)","واصف مواصفة USB (ليس السرعة المتفاوض عليها)",disk.usbSpecification());analyzer.field("Android device path","مسار جهاز أندرويد",d.getDeviceName());
        analyzer.field("Device class / subclass / protocol","فئة الجهاز / الفئة الفرعية / البروتوكول",d.getDeviceClass()+" / "+d.getDeviceSubclass()+" / "+d.getDeviceProtocol());
        for(int i=0;i<d.getConfigurationCount();i++){UsbConfiguration config=d.getConfiguration(i);analyzer.field("Configuration ID / declared maximum current (mA)","معرف الإعداد / أقصى تيار معلن (مللي أمبير)",config.getId()+" / "+config.getMaxPower());analyzer.field("Self-powered descriptor","واصف مصدر الطاقة الذاتي",config.isSelfPowered()?"Yes":"No",config.isSelfPowered()?"نعم":"لا");}
        analyzer.field("SCSI vendor / product / revision","مصنع SCSI / المنتج / المراجعة",disk.inquiryIdentity());
        analyzer.field("Transport / addressed LUN","النقل / الوحدة المنطقية المعنونة","BOT / SCSI / LUN0");
        for(int i=0;i<d.getInterfaceCount();i++){UsbInterface f=d.getInterface(i);analyzer.field("Interface ID / class / subclass / protocol","معرف الواجهة / الفئة / الفئة الفرعية / البروتوكول",f.getId()+" / "+f.getInterfaceClass()+" / "+f.getInterfaceSubclass()+" / "+f.getInterfaceProtocol());for(int j=0;j<f.getEndpointCount();j++){UsbEndpoint ep=f.getEndpoint(j);analyzer.field("Endpoint address / type / maximum packet / interval","عنوان نقطة النهاية / النوع / أقصى حزمة / الفاصل",ep.getAddress()+" / "+ep.getType()+" / "+ep.getMaxPacketSize()+" / "+ep.getInterval());}}
        return analyzer.analyze();
    }
    private static long u32(byte[] b,int at){return (b[at]&255L)|((b[at+1]&255L)<<8)|((b[at+2]&255L)<<16)|((b[at+3]&255L)<<24);}
}
