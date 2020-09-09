package com.Ray.Bicycle;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.widget.TextView;

import com.github.ivbaranov.rxbluetooth.exceptions.ConnectionClosedException;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import java.util.logging.LogRecord;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class RxOkHttp3 {
    private String GetVal;
    double Long,Lat;
    private LatLng Location;
    private Flowable<LatLng> LongLat;
    private boolean ans = false;
    Context context;


    private int m_nTime = 0;
    private Handler mHandlerTime = new Handler();
     //mHandlerTime.postDelayed(timerRun, 1000);

    private final Runnable timerRun = new Runnable()
    {
        public void run()
        {
            ++m_nTime; // 經過的秒數 + 1
            mHandlerTime.postDelayed(this, 1000);
            // 若要取消可以寫一個判斷在這決定是否啟動下一次即可
        }
    };

    protected LatLng sendGET() {

        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                //.url("https://jsonplaceholder.typicode.com/posts/1")
                //.url("http://35.221.236.109:3000/getSetting")
                // .url("http://35.221.236.109:3000/getSetting/id123")
                //.url("http://35.221.236.109:3000/getGps/ID123456789ABC")
                .url("http://35.221.236.109:3000/getGps/ABC123")
                //資料庫測試        .url("http://35.221.236.109:3000/api880509")
                //.url("https://maker.ifttt.com/trigger/line/with/key/0nl929cYWV-nv9f76AW_O?value1=1")
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                // tvRes.setText(e.getMessage());
                ans = false;
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                // GetVal = response.body().string();
                GetVal = Objects.requireNonNull(response.body()).string();
                //  tvRes.setText("GET回傳：\n" + GetVal);
                ans = true;
                System.out.print("Get:");
                System.out.print(GetVal);
                try {
                    Location = getJson(GetVal);
                    System.out.println(Location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return Location;

    }
    public Flowable<LatLng> LocationStream() {
        if (LongLat == null) {
            LongLat =Flowable.create((FlowableOnSubscribe<LatLng>) subscriber -> {
                if (!subscriber.isCancelled()) {
                    //if(sendGET())
                    sendGET();
                    Location = getJson(GetVal);
                    //Location = new LatLng(24.922582, 121.422590);
                    subscriber.onNext(Location);

                }
            }, BackpressureStrategy.BUFFER).share();
        }
        return LongLat;
    }

    /**getJson**/
    protected LatLng getJson(String json) throws JSONException {
        JSONArray ary = new JSONArray(json);
        /*for (int i = 0; i < ary.length(); i++) {
            JSONObject objects = ary.getJSONObject(i);

            Iterator key = objects.keys();

            while (key.hasNext()) {
                String k = key.next().toString();
                System.out.println("Key : " + k + ", value : "
                        + objects.getString(k));
            }
            System.out.println("-----------");
        }*/
        System.out.println("LatLng:");
        double latitude = Double.parseDouble(ary.getJSONObject(ary.length()-1).getString("longitude"));
        double longitude = Double.parseDouble(ary.getJSONObject(ary.length()-1).getString("latitude"));
        System.out.println(latitude);
        System.out.println(longitude);
        LatLng locat = new LatLng(latitude,longitude);
        //LatLng locat = new LatLng(24.922582, 121.422590);
        System.out.println(locat);
        System.out.println("a");
        return locat;
    }

    /*@CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    public static Flowable<LatLng> interval(long initialDelay, long period, TimeUnit unit) {
        return interval(initialDelay, period, unit);
    }*/
}
