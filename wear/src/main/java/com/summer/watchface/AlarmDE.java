package com.summer.watchface;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.blankj.utilcode.util.LogUtils;

import java.io.Serializable;

public class AlarmDE implements Serializable {


    public void setAlarms(Context context){
        setAlarm(context,System.currentTimeMillis()+30*60*1000);
    }


    public void setAlarm(Context context, long time){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppReceiver.class);
        intent.putExtra("time",time);
        PendingIntent pendingIntent = null;
        pendingIntent = PendingIntent.getBroadcast(context, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //pendingIntent = PendingIntent.getActivity(context,0,new Intent(context,WelcomeCT.class),PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LogUtils.e("setAlarm1");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LogUtils.e("setAlarm2");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            LogUtils.e("setAlarm3");
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}
