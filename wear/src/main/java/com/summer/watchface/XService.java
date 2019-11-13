package com.summer.watchface;


import com.summer.watchface.data.net.ListData;

import retrofit2.Call;
import retrofit2.http.GET;

public interface XService {


    @GET("alarm/selectAllAlarms")
    Call<ListData<Alarm>> selectAllAlarms();

}