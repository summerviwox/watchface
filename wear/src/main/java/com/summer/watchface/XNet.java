package com.summer.watchface;

import com.summer.watchface.data.net.NetDataHelper;

public class XNet {

    private static XService xService;

    public static XService getInstance(){
        if(xService ==null){
            NetDataHelper.getInstance().init("http://222.186.36.75:8888/"+"record/");
            xService = NetDataHelper.getInstance().getRetrofit().create(XService.class);
        }
        return xService;
    }
}