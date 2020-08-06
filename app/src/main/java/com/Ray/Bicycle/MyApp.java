package com.Ray.Bicycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

public class MyApp extends Application {

    public static MyApp appInstance;
    public static synchronized MyApp getAppInstance(){
        return appInstance;
    }

    private SimpleDateFormat dateFormat;
    private int count = 0;
    /***Bluetooth***/
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket socket;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void afficher() {
        Toast.makeText(getBaseContext(), dateFormat.format(new Date()), 300).show();
        handler.postDelayed(runnable,1000);
    }

    public void startCount(){
        count++;
        Toast.makeText(getBaseContext(), Integer.toString(count), 300).show();
        handler.postDelayed(runnable,1000);
        if(count>=10){
            count=0;
        }
    }

    public void startTimer() {
        runnable.run();
    }

    public void BTConnect(String Name, String Adds, Button BTBut){
        if(Adds == null){
            BTBut.setText("未選擇裝置");
            return;
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(Adds);
        try {
            //loadingDialog.startLoadingDialog();
            BTBut.setText("連線中");
            socket = device.createRfcommSocketToServiceRecord(serialPortUUID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            BTBut.setText("已連線");

        } catch (IOException e) {
            e.printStackTrace();
            BTBut.setText("連線超時");
        }
    }

    public void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            //afficher();
            startCount();

        }
    };

}