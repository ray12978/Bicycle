package com.Ray.Bicycle;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.Address;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.annotation.Nullable;

public class HelloService extends Service {
    public byte[] buffer = new byte[256];
    /***Bluetooth***/
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address;
    private String name;
    public  StringBuffer BTValTmp = new StringBuffer();
    public synchronized StringBuffer getBTVal(){return BTValTmp; }
    private BluetoothSocket socket;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    private BluetoothAdapter bluetoothAdapter;
    public String BTStatus = null;
    private MyBinder mBinder;
    @Override
    public void onCreate() {
        // 僅初次建立時呼叫
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次startService時會呼叫
        Log.d("HelloService","onStartCommand Start");
        //String name = intent.getStringExtra("DeviceName");
        address = intent.getStringExtra("DeviceAddress");
        name = intent.getStringExtra("DeviceName");
        //BTConnect(address);
        Log.d("HelloService","onStartCommand End");
        BTStatus = BTConnect(address);
        //stopSelf();  // 停止Service

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new MyBinder();
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    /**test**/
    /*class FirstBinder extends Binder {
        public String getData(){
            return "test data";
        }
    }*/
    public class MyBinder extends Binder {
        public HelloService getService(){
            return HelloService.this;
        }
    }
    public String getServiceName(){
        return HelloService.class.getSimpleName();
    }
    public String getBTStatus(){
        return BTStatus;
    }
    public String getDeviceName(){
        if(name != null) return name;
        else return null;
    }
    public String getAddress(){
        if(address != null) return address;
        else return null;
    }

    /****/
    @Override
    public void onDestroy() {
        Log.d("HelloService", "onDestroy");
    }
    /**BlueTooth**/
    public String BTConnect(String Adds){
        if(Adds == null){
            return "未選擇裝置";
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(Adds);
        try {
            //loadingDialog.startLoadingDialog();
            socket = device.createRfcommSocketToServiceRecord(serialPortUUID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            return "已連線";

        } catch (IOException e) {
            e.printStackTrace();
            return "連線超時";
        }
    }

    public void disconnect(Button BTBut) {
        if (socket == null) return;

        try {
            socket.close();
            socket = null;
            inputStream = null;
            outputStream = null;
            BTBut.setText("未連線");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void BTSend(String BTMsg) {
        if (outputStream == null) return;

        try {
            //for(int i =0;i<5;i++) {
            outputStream.write(BTMsg.getBytes());
            outputStream.flush();
            // }
            System.out.print("BT:");
            System.out.print(BTMsg);
            System.out.print(",");
            System.out.print(BTMsg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Save_Val(@NotNull StringBuffer StrBufTmp) {
        if (inputStream == null) return;
        try {
            // if (inputStream.available() <= 0)return;
            String a = new String(buffer, 0, inputStream.read(buffer));
            StrBufTmp.append(a);
            //Thread.sleep(100);
            System.out.println("BT:");
            System.out.print(BTValTmp);
        } catch (IOException /*| InterruptedException*/ e) {
            e.printStackTrace();
        }
    }

    public void startListenBT(){
        Toast.makeText(getBaseContext(), "BTListening...", Toast.LENGTH_LONG).show();
        Save_Val(BTValTmp);
    }

}
