package com.Ray.Bicycle.RxJava;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.ContentValues.TAG;

public class RxOkHttp3 {
    private String GetVal, FallVal, Fall;
    private LatLng Location;
    private String preFallDateTime;
    private boolean FallReturn = false;
    String IP = "http://35.229.164.35:3000";

    /**
     * GET
     **/

    public LatLng getLocation(String id) {
        String url = IP + "/getGps/" + id;
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
    public interface FallCallback {
        void onOkHttpResponse(String data);

        void onOkHttpFailure(Exception exception);
    }

    /**
     * get Fall Msg
     **/
    public String getFallMsg(String id, FallCallback callback) {
        FallReturn = false;
        String url = IP + "/getSetting/" + id;
        // http://35.229.164.35:3000/getSetting/
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
                    if (Fall.equals("null")) {
                        System.out.println("Fall null return");
                    }
                    System.out.println(Fall);
                    FallReturn = true;
                    if (Fall != null)
                        callback.onOkHttpResponse(Fall);
                    else callback.onOkHttpResponse("AA");

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onOkHttpResponse(e.toString());
                }

            }
        });
        if (FallReturn) return Fall;
        else return "null";
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

    public void PostVal(String id, String SVal, String MVal, String TVal, String PVal) {
        if (SVal == null || MVal == null || TVal == null || PVal == null || id == null) {
            System.out.println("Val null, return");
            return;
        }///**nowtest**/
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        String url = IP + "/api880509";
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
                //.url("https://jsonplaceholder.typicode.com/posts")//資料庫測試
                .url(url)
                .addHeader("Content-Type", "x-www-form-urlencoded")
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
        String AT = ary.getJSONObject(ary.length() - 1).getString("beebee");
        String date = ary.getJSONObject(ary.length() - 1).getString("datetime");
        System.out.println("Fall:" + FallMsg);
        System.out.println("AT:" + AT);
        System.out.println("date:" + date);
        if (!date.equals(preFallDateTime)) {
            preFallDateTime = date;
            return FallMsg + AT;
        } else {
            System.out.println("Fall,AT date same return");
            return "null";
        }
    }
}

