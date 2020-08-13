package com.Ray.Bicycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

public class MyApp extends Application {

    public static MyApp appInstance;
    public static synchronized MyApp getAppInstance(){
        return appInstance;
    }
    public byte[] buffer = new byte[256];
    private SimpleDateFormat dateFormat;
    private int count = 0;
    /***Bluetooth***/
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public StringBuffer BTValTmp = new StringBuffer();
    public synchronized StringBuffer getBTVal(){return BTValTmp; }
    private BluetoothSocket socket;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    private BluetoothAdapter bluetoothAdapter;
    public FlagAddress BTConnStatus = new FlagAddress(false);
    public String DevAddress,DevName;
    /**Service**/
    private HelloService mService;
    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



    }

    public void afficher() {
        //Toast.makeText(getBaseContext(), dateFormat.format(new Date()), 300).show();
        handler.postDelayed(runnable,1000);
    }

    public void startCount(){
        count++;
        //Toast.makeText(getBaseContext(), Integer.toString(count), 300).show();
        handler.postDelayed(runnable,1000);
        if(count>=10){
            count=0;
        }
    }

    public void startTimer() {
        runnable.run();
    }
    /**BlueTooth**/
    public void BTConnect(String Name,String Adds, Button BTBut){
        if(Adds == null){
            BTBut.setText("未選擇裝置");
            BTConnStatus.Flag = false;
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
            BTConnStatus.Flag = true;
            DevAddress = Adds;
            DevName = Name;

        } catch (IOException e) {
            e.printStackTrace();
            BTBut.setText("連線超時");
            BTConnStatus.Flag = false;
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

    public String getDevAddress(){
        if(BTConnStatus.Flag)return DevAddress;
        else return null;
    }
    public String getDevName(){
        if(BTConnStatus.Flag)return DevName;
        else return null;
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
        count++;
        Toast.makeText(getBaseContext(), Integer.toString(count), Toast.LENGTH_LONG).show();
        Save_Val(BTValTmp);
        handler.postDelayed(runnable,1000);
        if(count>=10){
            count=0;
        }

    }

    public void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            //afficher();
            //startCount();
            //startListenBT();
        }
    };

}