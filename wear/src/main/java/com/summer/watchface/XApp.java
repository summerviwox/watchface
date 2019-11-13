package com.summer.watchface;

import android.app.Application;
import android.content.Context;



public class XApp extends Application {

    private static XApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static XApp getInstance(){
        return instance;
    }


}