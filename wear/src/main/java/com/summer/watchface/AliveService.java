package com.summer.watchface;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;


import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

import java.util.concurrent.TimeUnit;

public class AliveService extends Service implements OnFinishI {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private static MediaPlayer mediaPlayer = null;

    int i=0;

    Handler handler = new Handler();

    private static final long 更新时间 = TimeUnit.MINUTES.toMillis(1);
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //ToastUtils.showLong("111111111111111111111111111");
        AppReceiver.unRegist(getApplicationContext());
        AppReceiver.regist(getApplicationContext());
        new AlarmDE().setAlarms(getApplicationContext());
//        if(mediaPlayer==null){
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.minute);
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    if(mediaPlayer!=null){
//                        mediaPlayer.start();
//                        onFinished(mp);
//                    }
//                }
//            });
//            //mediaPlayer.setLooping(true);
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mediaPlayer.start();
//                }
//            });
//        }
        //onFinished(0);
        recyle();
        NotificationManager notificationManager = (NotificationManager) XApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("summer-record", "summer", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), "summer-record").build();
            startForeground(1, notification);
        }else{
            startForeground(1, new Notification());
        }
        return START_STICKY;
    }
    public void recyle(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onFinished(i);
            }
        },更新时间 - System.currentTimeMillis()%(更新时间));
    }


    @Override
    public void onFinished(Object o) {
        LogUtils.e("onFinished");
        Intent intent = new Intent("com.summer.appreceiver");
        intent.putExtra("num",20000+(i/6));
        sendBroadcast(intent);
        recyle();
    }


    public static void startService(Context context){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,AliveService.class));
        } else {
            context.startService(new Intent(context,AliveService.class));
        }
    }

    public static void stopService(Context context){
        context.stopService(new Intent(context,AliveService.class));
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}