package com.Ray.Bicycle;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class HelloService extends Service {

    @Override
    public void onCreate() {
        // 僅初次建立時呼叫
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次startService時會呼叫
        Log.d("HelloService","onStartCommand Start");
        long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
        Log.d("HelloService","onStartCommand End");

        stopSelf();  // 停止Service

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("HelloService", "onDestroy");
    }
}
