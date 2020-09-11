package com.Ray.Bicycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyApp extends Application {

    public static MyApp appInstance;
   // public static MainActivity mainActivity = new MainActivity();
    public static synchronized MyApp getAppInstance() {
        return appInstance;
    }

    public byte[] buffer = new byte[256];
    private SimpleDateFormat dateFormat;
    public int count = 0;
    /***Bluetooth***/
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public StringBuffer BTValTmp = new StringBuffer();
    public synchronized StringBuffer getBTVal() {
        return BTValTmp;
    }
    private BluetoothSocket socket;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    private BluetoothAdapter bluetoothAdapter;
    public FlagAddress BTRevSta = new FlagAddress(false);
    public String DevAddress, DevName;
    private String TAG = "BTSta";
    /**RxBluetooth**/
    BluetoothConnection blueConn;
    RxBluetooth rxBluetooth = new RxBluetooth(this);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    AtomicInteger readCnt = new AtomicInteger();
    //private int i = 0;
    /** ConnectActivity Object**/
    ConnectActivity ConnAct = new ConnectActivity();

    /**String**/
    public String SVal,MVal,danger;
    private int[] StrPosition = new int[4];

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //inputStream = rxBluetooth.observeConnectionState()


    }


    public boolean getBTState() {
        return socket != null;
    }


    /**
     * BlueTooth
     **/

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
    protected void SavByte(int count,byte BTByte){
         buffer[count] = BTByte;
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

    public void Save_Val(@NotNull StringBuffer StrBufTmp,int count) {
        if (buffer == null) return;
        // if (inputStream.available() <= 0)return;
        String a = new String(buffer, 0, count+1);
        StrBufTmp.replace(0,count+1,a);
        //Thread.sleep(100);
        System.out.print("BTValTmp:");
        System.out.println(BTValTmp);

    }
    public void str_process() {
        int b = 0;
        //BTValTmp = new StringBuffer("S123M456T789P147"); //test
        //BTValTmp = new StringBuffer("B1"); //test
        if(BTValTmp.length() == 0)return;
        if (BTValTmp.toString().charAt(0) == 'S') {
            for (int i = 0; i < BTValTmp.length(); i++) {
                if (BTValTmp.toString().getBytes()[i] > 57) {
                    StrPosition[b] = i;
                    b++;
                }
            }
            SVal = BTValTmp.toString().substring(StrPosition[0] + 1, StrPosition[1]).trim();
            MVal = BTValTmp.toString().substring(StrPosition[1] + 1, StrPosition[2]).trim();
            danger = BTValTmp.toString().substring(BTValTmp.length()-1, BTValTmp.length()).trim();
            //PVal = BTValTmp.toString().substring(StrPosition[3] + 1).trim();
            Log.e("Tmp", BTValTmp.toString());
            Log.e("S", SVal);
            Log.e("M", MVal);
            Log.e("danger", danger);
            //Log.e("P", PVal);
            //BTValTmp.delete(0, BTValTmp.length());
        } /*else if (BTValTmp.toString().charAt(0) == 'B') {
            String Status = BTValTmp.toString().substring(1, 2).trim();
            BTValTmp.delete(0, BTValTmp.length());
            DanFlag.Flag = Status.equals("1");
            System.out.println(DanFlag.Flag);
            //if (DanFlag.Flag) Danger_Msg();
        }*/
    }
    public String getVal(char Select){
        if(SVal==null || MVal==null || danger==null)
            return null;
        switch (Select){
            case 'S':
                return SVal;
            case 'M':
                return MVal;
            case 'T':
                return danger;
            case 'A':
                return BTValTmp.toString();
        }
        return "null";
    }





    protected boolean connDevice(BluetoothDevice device) {
        AtomicBoolean Sta = new AtomicBoolean(false);
        compositeDisposable.add(rxBluetooth.connectAsClient(device, serialPortUUID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        bluetoothSocket -> {
                            // Connected to bluetooth device, do anything with the socket
                            System.out.println("conned");
                            socket = bluetoothSocket;
                            ReadBT();
                            Sta.set(true);
                        }, throwable -> {
                            // On error
                            System.out.println("error");
                            Sta.set(false);
                            //System.out.println(ConnAct.getDevice().getName());
                            //System.out.println(ConnAct.getDevice().getAddress());
                        }));
        return Sta.get();
    }
    private void ReadBT() throws Exception {

        BluetoothConnection bluetoothConnection = new BluetoothConnection(socket);
        compositeDisposable.add(bluetoothConnection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aByte -> {
                    //buffer[i] = aByte;
                    SavByte(readCnt.get(),aByte);
                    Save_Val(BTValTmp,readCnt.get());
                    readCnt.getAndIncrement();
                    BTRevSta.Flag = true;
                    // This will be called every single byte received
                    System.out.print("Recv byte:");
                    System.out.println(aByte);

                    //SavByte(aByte);
                    //System.out.println(Arrays.toString(buffer));
                    //System.out.println(buffer[buffer.length-1]);
                }, throwable -> {
                    BTRevSta.Flag = false;
                    // Error occured
                    System.out.println("Recv byte Error");
                }));
        BTRevSta.Flag = false;
    }


    protected void writeBT(String Msg) throws Exception {
        buffer = new byte[256];
        readCnt = new AtomicInteger();
        BTValTmp.delete(0, BTValTmp.length());
        System.out.println("BTValTmp:"+BTValTmp);
        System.out.println("buffer:"+ Arrays.toString(buffer));
        BluetoothConnection bluetoothConnection = new BluetoothConnection(socket);
        bluetoothConnection.send(Msg); // String
        System.out.println("Now Send:" + Msg);
        Toast.makeText(this, "Now Send:" + Msg, Toast.LENGTH_SHORT).show();
    }

}