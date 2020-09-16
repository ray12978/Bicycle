package com.Ray.Bicycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyApp extends Application {

    public static MyApp appInstance;

    //public MainActivity mainActivity = new MainActivity();
    public static synchronized MyApp getAppInstance() {
        return appInstance;
    }

    public byte[] buffer = new byte[256];
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
    public FlagAddress DangerFlag = new FlagAddress(true);
    public FlagAddress BTRevFlag = new FlagAddress(false);
    public String DevAddress, DevName;
    private String TAG = "BTSta";
    /**
     * RxBluetooth
     **/
    BluetoothConnection blueConn;
    RxBluetooth rxBluetooth = new RxBluetooth(this);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    AtomicInteger readCnt = new AtomicInteger();
    //private int i = 0;
    /**
     * ConnectActivity Object
     **/
    ConnectActivity ConnAct = new ConnectActivity();

    /**
     * String
     **/
    public String SVal, MVal, danger;
    private int[] StrPosition = new int[4];

    /**
     * NOTIFY
     **/
    private static final String TEST_NOTIFY_ID = "Bicycle_Danger_1";
    private static final int NOTIFY_REQUEST_ID = 300;
    //Context context;
    /**
     * Timer
     */
    private int count = 0;

    /**
     * RxDanger
     */
    Subscription mSubscription;



   public void ScanDanger(/*AlertDialog Dia*/){  //Temporarily reserved
       RxDanger rxDanger = new RxDanger();
        compositeDisposable.add(rxDanger.RxDangerStream("A")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    if(s.equals("A"))
                        System.out.println("recv:A");
                    //Dia.show();
                }, throwable -> {
                    //BTRevSta.Flag = false;
                    // Error occured
                    System.out.println("Recv Danger Error");
                }));


    }
    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //inputStream = rxBluetooth.observeConnectionState()


    }
    /****/
    public void afficher() {
        //Toast.makeText(getBaseContext(), dateFormat.format(new Date()), 300).show();
        handler.postDelayed(runnable,1000);
    }

    public void startCount(){
        //count++;
        //System.out.print("count:");
        //System.out.println(count);
        if(BTRevSta.Flag)str_process();
        //if()
        DangerNow();

        //Toast.makeText(getBaseContext(), Integer.toString(count), 300).show();
        handler.postDelayed(runnable,1000);
        /*if(count>=10){
            count=0;
        }*/
    }

    public void startTimer() {
        runnable.run();
    }
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            //afficher();
            startCount();

        }
    };


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

    protected void SavByte(int count, byte BTByte) {
        buffer[count] = BTByte;
    }


    public void Save_Val(@NotNull StringBuffer StrBufTmp, int count) {
        if (buffer == null) return;
        // if (inputStream.available() <= 0)return;
        String a = new String(buffer, 0, count + 1);
        StrBufTmp.replace(0, count + 1, a);
        //Thread.sleep(100);
        System.out.print("BTValTmp:");
        System.out.println(BTValTmp);

    }

    public void str_process() {
        int b = 0;
        //BTValTmp = new StringBuffer("S123M456T789P147"); //test
        //BTValTmp = new StringBuffer("B1"); //test
        if (BTValTmp.length() == 0) return;
        if (BTValTmp.toString().charAt(0) == 'S') {
            for (int i = 0; i < BTValTmp.length(); i++) {
                if (BTValTmp.toString().getBytes()[i] > 57) {
                    StrPosition[b] = i;
                    b++;
                }
            }
            SVal = BTValTmp.toString().substring(StrPosition[0] + 1, StrPosition[1]).trim();
            MVal = BTValTmp.toString().substring(StrPosition[1] + 1, StrPosition[2]).trim();
            danger = BTValTmp.toString().substring(BTValTmp.length() - 1, BTValTmp.length()).trim();
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

    public String getVal(char Select) {
        if (SVal == null || MVal == null || danger == null)
            return null;
        switch (Select) {
            case 'S':
                return SVal;
            case 'M':
                return MVal;
            case 'D':
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
                    SavByte(readCnt.get(), aByte);
                    Save_Val(BTValTmp, readCnt.get());
                    readCnt.getAndIncrement();
                    BTRevSta.Flag = true;
                    // This will be called every single byte received
                    System.out.print("Recv byte:");
                    System.out.println(aByte);
                    BTRevFlag.Flag = true;
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
        System.out.println("BTValTmp:" + BTValTmp);
        System.out.println("buffer:" + Arrays.toString(buffer));
        BluetoothConnection bluetoothConnection = new BluetoothConnection(socket);
        bluetoothConnection.send(Msg); // String
        System.out.println("Now Send:" + Msg);
        Toast.makeText(this, "Now Send:" + Msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 手機震動
     **/
    public void setVibrate(int time) {
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    void DangerNow() {
        if (getVal('D') == null) {
            System.out.println("D is null will be end");
            return;
        }
        String codi = getVal('D');
        if (codi.equals("Y")) {
            //setVibrate(1000);
            //DanFlag.Flag = false;
            if(DangerFlag.Flag)showNotification();
            System.out.println(DangerFlag.Flag);
            //mainActivity.Danger_Msg();
            //mediaPlayer.start();
            //if(Alert.Flag)Danger_Msg();
            //loadingDialog.startLoadingDialog();
        }
    }

    public void showNotification() {
        Log.d(TAG, "showNotification: ");
        try {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    NOTIFY_REQUEST_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            //PendingIntent MutePend =
            final Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.sound);
            System.out.println(soundUri);
            //final Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            Notification.Action action = new Notification.Action.Builder(0,"確定",pendingIntent).build();
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("您的腳踏車發生異常狀況")
                    .setContentText("請立即前往確認")
                    .setLights(0xff00ff00, 300, 1000)
                    .setSmallIcon(R.drawable.ic_baseline_warning_48)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSound(soundUri, audioAttributes)
                    .setContentIntent(pendingIntent)
                    //.addAction(action);
                    .setAutoCancel(true);

            NotificationChannel channel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(TEST_NOTIFY_ID
                        , "Danger Msg"
                        , NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.shouldShowLights();
                channel.setLightColor(Color.GREEN);
                channel.setSound(soundUri, audioAttributes);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                builder.setChannelId(TEST_NOTIFY_ID);

                manager.createNotificationChannel(channel);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            int testNotifyId = 12;
            manager.notify(testNotifyId,
                    builder.build());
        } catch (Exception e) {

        }
    }


}