package com.Ray.Bicycle;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.ivbaranov.rxbluetooth.exceptions.ConnectionClosedException;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import kotlin.random.FallbackThreadLocalRandom;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.ContentValues.TAG;

public class RxOkHttp3 {
    private String GetVal, FallVal, Fall, ATVal, AT;
    private LatLng Location;
    private Date date;
    private String preATDateTime;
    private String preFallDateTime;
    private boolean FallReturn = false;
    /**
     * GET
     **/

    protected LatLng getLocation(String id) {
        String url = "http://35.221.236.109:3000/getGps/" + id;
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(url)
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                GetVal = Objects.requireNonNull(response.body()).string();
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
    /**
     * interface
     */
    interface FallCallback{
        void onOkHttpResponse(String data);
        void onOkHttpFailure(Exception exception);
    }
    /**
     * get Fall Msg
     **/
    protected String getFallMsg(String id, FallCallback callback) {
        FallReturn = false;
        String url = "http://35.221.236.109:3000/getSetting/" + id;
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(url)
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
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                FallVal = Objects.requireNonNull(response.body()).string();
                System.out.print("GetFall:");
                System.out.print(FallVal);
                try {
                    Fall = getFallJson(FallVal);
                    if(Fall.equals("null")){
                        System.out.println("Fall null return");
                    }
                    System.out.println(Fall);
                    FallReturn = true;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        if(FallReturn)return Fall;
        else return "null";
    }

    /**
     * Get AntiTheft Msg
     **/
    protected String getATMsg(String id) {
        String url = "http://35.221.236.109:3000/getSetting/" + id;
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(url)
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                ATVal = Objects.requireNonNull(response.body()).string();
                System.out.print("GetAT:");
                System.out.print(ATVal);
                try {
                    AT = getATJson(ATVal);
                    if(AT.equals("null")){
                        System.out.println("AT null");
                    }
                    System.out.println(AT);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return AT;
    }

    /**
     * POST
     **/
    void displayVal(String id, String SVal, String MVal, String TVal, String PVal) {
        Log.d(TAG, "id: " + id);
        Log.d(TAG, "SVal: " + SVal);
        Log.d(TAG, "MVal: " + MVal);
        Log.d(TAG, "TVal: " + TVal);
        Log.d(TAG, "PVal: " + PVal);
    }

    protected void PostVal(String id, String SVal, String MVal, String TVal, String PVal) {
        if (SVal == null || MVal == null || TVal == null || PVal == null || id == null) {
            System.out.println("Val null, return");
            return;
        }///**nowtest**/
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        int topspeed = 0;

        FormBody formBody = new FormBody.Builder()
                /* .add("id", id)
                 .add("speed", "6")
                 .add("miliage", "3000")
                 .add("exercise", "350")
                 .add("topspeed", "20")/**nowtest**/
                .add("id", id)
                .add("speed", SVal) //nowSpeed
                .add("miliage", MVal) //mileage
                .add("exercise", TVal) //Time
                .add("topspeed", PVal) //topSpeed
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                //.url("https://jsonplaceholder.typicode.com/posts")
                .url("http://35.221.236.109:3000/api880509")//資料庫測試
                .addHeader("Content-Type", "x-www-form-urlencoded")
                //.url("https://maker.ifttt.com/trigger/line/with/key/0nl929cYWV-nv9f76AW_O?value1=2")
                .post(formBody)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                System.out.println(e.getMessage());
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                //tvRes.setText("POST回傳：\n" + response.body().string());
                System.out.println("POST回傳：\n" + response.body().string());
                System.out.println("id:" + id);
            }
        });
    }

    /**
     * getJson
     **/
    protected LatLng getJson(String json) throws JSONException {
        JSONArray ary = new JSONArray(json);
        System.out.println("LatLng:");
        double latitude = Double.parseDouble(ary.getJSONObject(ary.length() - 1).getString("latitude"));
        double longitude = Double.parseDouble(ary.getJSONObject(ary.length() - 1).getString("longitude"));
        System.out.println(latitude);
        System.out.println(longitude);
        LatLng locat = new LatLng(latitude, longitude);
        System.out.println(locat);
        System.out.println("a");
        return locat;
    }

    protected String getFallJson(String json) throws JSONException {
        JSONArray ary = new JSONArray(json);
        System.out.println("FallVal:");
        String FallMsg = ary.getJSONObject(ary.length() - 1).getString("fall");
        String AT = ary.getJSONObject(ary.length() - 1).getString("fall");
        String date = ary.getJSONObject(ary.length() - 1).getString("datetime");
        System.out.println("Fall:" + FallMsg);
        System.out.println("AT:" + AT);
        System.out.println("date:" + date);
        if (!date.equals(preFallDateTime)) {
            preFallDateTime = date;
            return FallMsg+AT;
        } else {
            System.out.println("Fall,AT date same return");
            return "null";
        }
    }

    protected String getATJson(String json) throws JSONException {
        JSONArray ary = new JSONArray(json);
        System.out.println("FallVal:");
        String FallMsg = ary.getJSONObject(ary.length() - 1).getString("beebee");
        String date = ary.getJSONObject(ary.length() - 1).getString("datetime");
        System.out.println("beebee:" + FallMsg);
        System.out.println("date:" + date);
        if (!date.equals(preFallDateTime)) {
            preFallDateTime = date;
            return FallMsg;
        } else{
            System.out.println("AT date same return");
            return "null";
        }
    }
}

