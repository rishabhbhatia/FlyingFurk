package com.satiate.flyingfurk;

import android.app.Application;
import android.util.Log;

import com.satiate.flyingfurk.retrofit.FurkNetworkService;
import com.satiate.flyingfurk.utils.Const;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rishabh Bhatia on 28/10/16.
 */

public class FurkApplication extends Application {

    public static Retrofit retrofit = null;
    public static FurkNetworkService furkNetworkService = null;
    public static OkHttpClient.Builder httpClient = null;


    @Override
    public void onCreate() {
        super.onCreate();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(Const.RETROFIT_NETWORK_CALL_TIMEOUT, TimeUnit.SECONDS);
        httpClient.writeTimeout(Const.RETROFIT_NETWORK_CALL_TIMEOUT, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(Const.NETWORK_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        furkNetworkService = retrofit.create(FurkNetworkService.class);
    }
}
