package com.summer.watchface.data.net;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseCallBack<T> implements Callback<T> {


    public void onSuccess(T t){

    }

    public void onError(int code,String error){

    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
           onSuccess(response.body());
        }else{
            onError(response.code(),response.message());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onError(0,t.getMessage());
    }
}
