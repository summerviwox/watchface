package com.summer.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;


import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.summer.watchface.data.net.BaseCallBack;
import com.summer.watchface.data.net.ObjectData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class AppReceiver extends BroadcastReceiver {

    private static AppReceiver appReceiver;

    public static Vibrator vibrator;

    int i=0;
    public final long[] VIBRATE_TIME = new long[]{
            1000,3000,1000,3000,1000,3000,1000,3000,
    };
    public static ArrayList<Alarm> alarms =new ArrayList<>();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("1111","222222");
        update(context);
    }

    public static void regist(Context context){
        if(appReceiver==null){
            appReceiver = new AppReceiver();
        }
        IntentFilter intentFilter = new IntentFilter("com.summer.appreceiver");
        context.registerReceiver(appReceiver,intentFilter);
    }

    public static void unRegist(Context context){
        if(appReceiver!=null){
            context.unregisterReceiver(appReceiver);
        }
    }

    private void update(Context context) {
        Crash crash = new Crash();
        crash.setCreatedtime(System.currentTimeMillis());
        crash.setError("");
        crash.setPlatform("android");
        crash.setUser(Build.DEVICE);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        crash.setTimestr(sd.format(new Date()));
        XNet.getInstance().sendCrash(GsonUtils.toJson(crash)).enqueue(new BaseCallBack<ObjectData<Boolean>>(){});

        LogUtils.e("updateupdateupdateupdate");
        int b = checked(alarms);
        if (b != -1) {
            if (vibrator == null) {
                vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(VIBRATE_TIME,-1);
                vibrator.vibrate(vibrationEffect);
            }else{
                vibrator.vibrate(VIBRATE_TIME,-1);
            }
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        int c = checkedArea(alarms);
        if (c != -1) {
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            MyWatchFace.现在显示内容 = "" + alarms.get(c).getText() + "(" + simpleDateFormat.format(new Date(alarms.get(c).getStarttime())) + "--" + simpleDateFormat.format(new Date(alarms.get(c).getEndtime())) + ")";
        }else{
            MyWatchFace.现在显示内容 = "空闲";
        }
        int next = checkedNext(alarms);
        if(next!=-1){
            MyWatchFace.接下来显示内容 = alarms.get(next).getText() + "(" + simpleDateFormat.format(new Date(alarms.get(next).getStarttime())) + "--" + simpleDateFormat.format(new Date(alarms.get(next).getEndtime())) + ")";
        }else{
            MyWatchFace.接下来显示内容 = "空闲";
        }

//        BatteryManager batteryManager = (BatteryManager)context.getSystemService(Context.BATTERY_SERVICE);
//        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//        MyWatchFace.电池电量 = battery+"%";

        if(next !=-1){
            long time = (TimeUnit.DAYS.toMillis(1)*
                    (System.currentTimeMillis()/TimeUnit.DAYS.toMillis(1)))+
                    (AppReceiver.alarms.get(next).getStarttime()%TimeUnit.DAYS.toMillis(1));
            new AlarmDE().setAlarm(context,time);
        }
    }


    public int checked(ArrayList<Alarm> datas){
        int  b= -1;
        Calendar calendar = Calendar.getInstance();
        int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
        for(int i=0;datas!=null&&i<datas.size();i++){
            if(nowmin==datas.get(i).getStart()){
                b= i;
            }
        }
        return b;
    }

    public int checkedArea(ArrayList<Alarm> datas){
        int  b= -1;
        Calendar calendar = Calendar.getInstance();
        int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
        for(int i=0;datas!=null&&i<datas.size();i++){
            if(nowmin>=datas.get(i).getStart()&&nowmin<=datas.get(i).getEnd()){
                b= i;
                return b;
            }
        }
        return b;
    }

    public int checkedNext(ArrayList<Alarm> datas){
        if(datas==null||datas.size()==0){
            return -1;
        }
        int  b= 0;
        Calendar calendar = Calendar.getInstance();
        int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
        for(int i=0;datas!=null&&i<datas.size();i++){
            // LogUtils.e(datas.get(i).getText());
            if(nowmin<=datas.get(i).getStart()){
                b= i;
                return b;
            }
        }
        return b;
    }
}
