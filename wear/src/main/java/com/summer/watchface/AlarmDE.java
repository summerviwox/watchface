package com.summer.watchface;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.Serializable;

public class AlarmDE implements Serializable {


    public void setAlarms(Context context){
        setAlarm(context,System.currentTimeMillis()+30*60*1000);
    }


    public void setAlarm(Context context, long time){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AliveService.class);
        intent.putExtra("time",time);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(context, (int) time,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }else{
            pendingIntent = PendingIntent.getService(context, (int) time,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        //pendingIntent = PendingIntent.getActivity(context,0,new Intent(context,WelcomeCT.class),PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 1000*60*60*24, pendingIntent);
        }
    }
}
