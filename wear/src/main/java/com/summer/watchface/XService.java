package com.summer.watchface;


import com.summer.watchface.data.net.ListData;
import com.summer.watchface.data.net.ObjectData;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface XService {


    @GET("alarm/selectAllAlarms")
    Call<ListData<Alarm>> selectAllAlarms();



    @FormUrlEncoded
    @POST("crash/sendCrash")
    Call<ObjectData<Boolean>> sendCrash(@Field("data") String data);

}