package com.summer.watchface.data.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求工具
 */
public class NetDataHelper {

    private static NetDataHelper instance;

    private Retrofit retrofit;

    public static NetDataHelper getInstance(){
        if(instance==null){
            instance = new NetDataHelper();
        }
        return instance;
    }


    public void init(String url){
        OkHttpClient client = new OkHttpClient.Builder().
//                addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request = chain.request();
//                        request.newBuilder().addHeader("mediaType","application/json");
//                        return chain.proceed(request);
//                    }
//                })
//                .
                        connectTimeout(60, TimeUnit.SECONDS).
                        readTimeout(600, TimeUnit.SECONDS).
                        writeTimeout(600, TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
