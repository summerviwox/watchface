package com.summer.watchface;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class MainAct extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        //new AlarmDE().setAlarm(getApplicationContext(),2*1000);
    }
}
