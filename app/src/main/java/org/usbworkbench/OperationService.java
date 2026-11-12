package org.usbworkbench;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;

public class OperationService extends Service {
    public static volatile OperationService instance;
    private PowerManager.WakeLock wake;
    private WorkbenchApp app;
    private volatile boolean executing=false;
    private volatile int latestStartId;
    private boolean receiverRegistered;
    private final Handler heartbeat=new Handler(Looper.getMainLooper());
    private final Runnable tick=new Runnable(){public void run(){if(!executing)return;update();heartbeat.postDelayed(this,1000);}};
    private final BroadcastReceiver detach=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){app.deviceEpoch++;if(app.running&&app.usbJob)app.cancelNow();android.hardware.usb.UsbDevice d=intent.getParcelableExtra(android.hardware.usb.UsbManager.EXTRA_DEVICE);if(d!=null&&d.getDeviceName().equals(app.selected))app.selected="";}};
    @Override public void onCreate(){super.onCreate();app=(WorkbenchApp)getApplication();instance=this;IntentFilter detached=new IntentFilter(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED);if(Build.VERSION.SDK_INT>=33)registerReceiver(detach,detached,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(detach,detached,getPackageName()+".INTERNAL_USB",null);receiverRegistered=true;NotificationManager manager=getSystemService(NotificationManager.class);if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(new NotificationChannel("operations","USB operations / عمليات USB",NotificationManager.IMPORTANCE_LOW));}
    private Notification notification(){
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,OperationService.class).setAction("cancel"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        int percent=app.total>0?(int)Math.min(100,100.0*app.done/app.total):0;
        String content=app.progressText();
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"operations"):new Notification.Builder(this);
        return builder.setSmallIcon(org.usbworkbench.R.drawable.ic_usb).setContentTitle(app.tr("USB Workbench • ","ورشة USB • ")+app.jobTitle()).setContentText(content).setStyle(new Notification.BigTextStyle().bigText(content)).setContentIntent(open).setOngoing(true).setProgress(100,percent,app.total<=0).addAction(new Notification.Action.Builder(null,app.tr("Cancel","إلغاء"),stop).build()).build();
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        latestStartId=startId;
        if(intent!=null&&"cancel".equals(intent.getAction())){app.cancelNow();if(!executing)stopSelfResult(startId);return START_NOT_STICKY;}
        if(executing)return START_NOT_STICKY;
        Runnable work=app.pending;if(work==null){stopSelf();return START_NOT_STICKY;}
        try { if(Build.VERSION.SDK_INT>=29)startForeground(1,notification(),app.usbJob?ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE:ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);else startForeground(1,notification());
        wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"USBWorkbench:operation");wake.acquire(6*60*60*1000L);
        } catch(RuntimeException e){if(wake!=null&&wake.isHeld())wake.release();app.failStart();stopForeground(true);stopSelf();return START_NOT_STICKY;}
        executing=true;heartbeat.post(tick);
        app.worker.execute(()->{try{work.run();}finally{
            synchronized(app){if(wake!=null&&wake.isHeld())wake.release();stopForeground(true);app.pending=null;executing=false;heartbeat.removeCallbacks(tick);stopSelfResult(latestStartId);app.running=false;}
        }});
        return START_NOT_STICKY;
    }
    public void update(){synchronized(app){if(!executing||!app.running)return;try{getSystemService(NotificationManager.class).notify(1,notification());}catch(RuntimeException ignored){/* Notification refresh must not interrupt an already-running disk operation. */}}}
    @Override public void onDestroy(){heartbeat.removeCallbacks(tick);if(executing)app.cancelNow();if(wake!=null&&wake.isHeld())wake.release();if(receiverRegistered){unregisterReceiver(detach);receiverRegistered=false;}if(instance==this)instance=null;super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onTimeout(int startId,int type){app.cancelNow();stopSelf();}
}
