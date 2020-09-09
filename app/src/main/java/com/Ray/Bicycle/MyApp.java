package com.Ray.Bicycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
    //private int i = 0;
    /** ConnectActivity Object**/
    ConnectActivity ConnAct = new ConnectActivity();

    /**String**/
    public String SVal,PVal,MVal,TVal;
    private int[] StrPosition = new int[4];

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //inputStream = rxBluetooth.observeConnectionState()


    }

    public void afficher() {
        //Toast.makeText(getBaseContext(), dateFormat.format(new Date()), 300).show();
        handler.postDelayed(runnable, 1000);
    }

    public void startCount() {
        count++;
        //Toast.makeText(getBaseContext(), Integer.toString(count), 300).show();
        handler.postDelayed(runnable, 1000);
        if (count >= 10) {
            count = 0;
        }
    }

    public boolean getBTState() {
        return socket != null;
    }

    public void startTimer() {
        runnable.run();
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
            TVal = BTValTmp.toString().substring(StrPosition[2] + 1, StrPosition[3]).trim();
            PVal = BTValTmp.toString().substring(StrPosition[3] + 1).trim();
            Log.d(BTValTmp.toString(), "Tmp");
            Log.d(SVal, "S");
            Log.d(MVal, "M");
            Log.d(TVal, "T");
            Log.d(PVal, "P");
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
        if(SVal==null || MVal==null || TVal==null || PVal==null)
            return null;
        switch (Select){
            case 'S':
                return SVal;
            case 'M':
                return MVal;
            case 'T':
                return TVal;
            case 'P':
                return PVal;
            case 'A':
                return BTValTmp.toString();
        }
        return "null";
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
    AtomicInteger i = new AtomicInteger();
        BluetoothConnection bluetoothConnection = new BluetoothConnection(socket);
        compositeDisposable.add(bluetoothConnection.observeByteStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aByte -> {
                    //buffer[i] = aByte;
                    SavByte(i.get(),aByte);
                    Save_Val(BTValTmp,i.get());
                    i.getAndIncrement();
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
        BluetoothConnection bluetoothConnection = new BluetoothConnection(socket);
        bluetoothConnection.send(Msg); // String
    }

}