package com.Ray.Bicycle.Util;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;

import com.Ray.Bicycle.Activity.ConnectActivity;
import com.Ray.Bicycle.Activity.MainActivity;
import com.Ray.Bicycle.R;
import com.Ray.Bicycle.RxJava.RxFallAlert;
import com.Ray.Bicycle.RxJava.RxOkHttp3;
import com.Ray.Bicycle.RxJava.RxPostTimer;
import com.Ray.Bicycle.RxJava.RxTimerUtil;
import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyApp extends Application {

    public static MyApp appInstance;

    public MainActivity mainActivity;

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
    public FlagAddress MuteFlag = new FlagAddress(false);
    public String DevAddress, DevName;
    private String TAG = "BTSta";
    /**
     * RxBluetooth
     **/
    BluetoothConnection blueConn;
    RxBluetooth rxBluetooth = new RxBluetooth(this);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    AtomicInteger readCnt = new AtomicInteger();
    RxOkHttp3 rxOkHttp3 = new RxOkHttp3();
    //private int i = 0;
    /**
     * ConnectActivity Object
     **/
    ConnectActivity ConnAct = new ConnectActivity();

    /**
     * String
     **/
    public String SVal, MVal, danger, PVal, TVal, id;
    private String codi;
    private int AllM;
    private int[] StrPosition = new int[5];
    private int BTMsgLen = 8;
    /**
     * NOTIFY
     **/
    private static final String TEST_NOTIFY_ID = "Bicycle_Danger_1";
    private static final int NOTIFY_REQUEST_ID = 300;
    com.Ray.Bicycle.Activity.Notification notification = new com.Ray.Bicycle.Activity.Notification();
    //Context context;
    /**
     * Timer
     */
    private int UnLockCnt = 0;
    /**
     * RxTimer
     */
    String BTSendMsg;
    private RxTimerUtil rxTimer = new RxTimerUtil();
    private RxPostTimer rxPostTimer = new RxPostTimer();
    private RxFallAlert rxFallAlert = new RxFallAlert();
    int cnt = 0;
    int preATMin = 0;
    int preFaMin = 0;
    boolean FState;
    private PostValue postValue;
    private final String[] all = new String[1];
    /**
     * SharedBTValue
     */
    private SharedPreferences BTShare;
    private SharedPreferences BTWrData;
    private SharedPreferences UserSetting;
    private SharedPreferences FallData;

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BTWrData = getSharedPreferences("BTMsg", MODE_PRIVATE);
        BTSendMsg = BTWrData.getString("SendMsg", "null");

        BTShare = getSharedPreferences("BTShare", MODE_PRIVATE);

        UserSetting = getSharedPreferences("UserSetting", MODE_PRIVATE);

        FallData = getSharedPreferences("FallData", MODE_PRIVATE);
        AllM = BTShare.getInt("Mi", 0);

        mainActivity = new MainActivity();

        postValue = new PostValue();
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

    protected void SavByte(int count, byte BTByte) {
        buffer[count] = BTByte;
    }


    public void Save_Val(@NotNull StringBuffer StrBufTmp, int count) {
        if (buffer == null) return;

        String a = new String(buffer, 0, count + 1);
        if (a.charAt(0) != 'S') return;
        StrBufTmp.replace(0, count + 1, a);
    }

    public void str_process() {
        int b = 0;
        //BTValTmp = new StringBuffer("S123M456T789P147"); //test
        //BTValTmp = new StringBuffer("B1"); //test
        if (BTValTmp.length() == 0) return;
        if (BTValTmp.toString().charAt(0) == 'S') {
            for (int i = 0; i < BTValTmp.length(); i++) {
                if (BTValTmp.toString().getBytes()[i] > 57/* && BTValTmp.toString().charAt(i)!='Y'&& BTValTmp.toString().charAt(i)!='N'*/) {
                    StrPosition[b] = i;
                    if (b != StrPosition.length - 1) b++;
                }
            }
            //System.out.println( BTValTmp.toString()+','+StrPosition[1]+','+StrPosition[2]);
            SVal = BTValTmp.toString().substring(StrPosition[0] + 1, StrPosition[1]).trim();
            MVal = BTValTmp.toString().substring(StrPosition[1] + 1, StrPosition[2]).trim();
            danger = BTValTmp.toString().substring(StrPosition[2], StrPosition[2] + 1).trim();
            TVal = Integer.toString(UserSetting.getInt("postTime", 15000) / 1000);
            PVal = UserSetting.getString("TopS", "0");
        }
    }

    public String getVal(char Select) {
        if (SVal == null || MVal == null || danger == null || TVal == null || PVal == null)
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
            case 'T':
                return TVal;
            case 'P':
                return PVal;
        }
        return "null";
    }


    public boolean connDevice(BluetoothDevice device) {
        AtomicBoolean Sta = new AtomicBoolean(false);
        compositeDisposable.add(rxBluetooth.connectAsClient(device, serialPortUUID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        bluetoothSocket -> {
                            // Connected to bluetooth device, do anything with the socket
                            System.out.println("conned");
                            //System.out.println( BTRevSta.Flag);
                            socket = bluetoothSocket;
                            ReadBT();
                            AutoWriteBT();
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
                    //System.out.print("Recv byte:");
                    //System.out.println(aByte);
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
        BluetoothConnection blueConn = new BluetoothConnection(socket);

        if (Msg.charAt(BTMsgLen - 7) == 'L' && Msg.charAt(BTMsgLen - 3) == 'T') {
            StringBuffer A = new StringBuffer();
            A.append(Msg);
            A.replace(BTMsgLen - 3, BTMsgLen - 2, "J");
            System.out.println("change T to J");
            Msg = A.toString();
        }
        blueConn.send(Msg); // String
        if (Msg.charAt(BTMsgLen - 7) == 'F') {
            UnLockCnt++;
            if (UnLockCnt >= 10) {
                String NMsg = Msg.replace('F', 'N');
                mainActivity.BTSendMsg.replace(BTMsgLen - 7, BTMsgLen - 6, "N");
                BTWrData.edit().putString("SendMsg", NMsg).apply();
                System.out.println("change F to N");
                UnLockCnt = 0;
            }
        }
        System.out.println("Now Send:" + Msg);
    }

    /**
     * 手機震動
     **/
   /* public void setVibrate(int time) {
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }*/

    void DangerNow() {
        if (getVal('D') == null) {
            //System.out.println("D is null will be end");
            return;
        }
        codi = getVal('D');
        if (codi.equals("Y")) {
            //setVibrate(1000);
            //DanFlag.Flag = false;
            boolean notiFlag = UserSetting.getBoolean("noti", false);
            if (DangerFlag.Flag && !MuteFlag.Flag && notiFlag) {
                ATNotification();
                //codi=null;
            }
            System.out.println(DangerFlag.Flag);
            //mainActivity.Danger_Msg();
            //mediaPlayer.start();
            //if(Alert.Flag)Danger_Msg();
            //loadingDialog.startLoadingDialog();
        }
    }

    private void SharedBTValue() {
        int MInt;
        if (MVal == null || MVal.equals("")) MInt = -1;
        else MInt = Integer.parseInt(MVal) * 121;
        //int MInt = MVal == null?-1:Integer.parseInt(MVal);
        if (MInt != -1) AllM = MInt + AllM;
        BTShare.edit()
                .putString("S", SVal)
                .putString("M", MVal)
                .putInt("Mi", AllM)
                .apply();
        System.out.println("Sval:" + SVal);
        System.out.println("Mval:" + MVal);
        System.out.println("ALLMval:" + AllM);
        //System.out.println("Shared BTval!");
    }


    /**
     * AutoWriteBT
     */
    public void AutoWriteBT() {

        rxTimer.interval(200, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(Object number) {
                // Log.e("home_show_three", "======MainActivity======" + number);
                sub();
                // System.out.println(number);
            }
        });
    }

    ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> emitter) {
            if (BTRevFlag.Flag) {

                if (BTSendMsg == null) return;
                BTSendMsg = BTWrData.getString("SendMsg", "null");
                if (BTSendMsg.equals("null")) {
                    //System.out.println("Msg null");
                    return;
                }

                emitter.onNext(BTSendMsg);
            }

            //System.out.println("信號發射：onComplete");
        }
    };
    /**
     * 创建被观察者，并带上被观察者的订阅
     */
    Observable<String> observable = Observable.create(observableOnSubscribe);

    final Disposable[] disposable = new Disposable[1];

    Observer<String> observer = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            //System.out.println("已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(String string) {
            try {
                if (readCnt.get() >= 9) {
                    writeBT(string);
                    buffer = new byte[256];
                    readCnt = new AtomicInteger();
                    str_process();
                    BTValTmp.delete(0, BTValTmp.length());
                    SharedBTValue();
                }
                boolean BTConnSta = UserSetting.getBoolean("btsta", false);
                if (BTConnSta) DangerNow();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(Throwable e) {
            // System.out.println("信号接收：onError " + e.getMessage());
            cancel();
        }

        @Override
        public void onComplete() {
            //System.out.println("信号接收：onComplete");
        }
    };

    public void sub() {
        //System.out.println("開始訂閱：subscribe");
        observable.subscribe(observer);
    }

    public void cancel() {
        System.out.println("取消訂閱：unsubscribe");
        if (disposable[0] != null)
            disposable[0].dispose();
    }

    /**
     * AutoPost
     **/
    public void AutoPostVal() {
        boolean PhFlag = UserSetting.getBoolean("ph", false);
        boolean ClFlag = UserSetting.getBoolean("cloud", false);
        if (!PhFlag || !ClFlag) {
            System.out.println("ph is close will return");
            return;
        }
        int postTime;
        postTime = UserSetting.getInt("postTime", 15000);
        rxPostTimer.interval(postTime, number -> {
            //Log.e("home_show_three", "======MainActivity======" + number);
            int Mile = BTShare.getInt("Mi", 0);
            int preMile = BTShare.getInt("preM", 0);
            String distance = String.valueOf(Mile - preMile);
            System.out.println("PostTime:" + postTime);
            getID();
            System.out.println("Mile:" + Mile);
            System.out.println("preMile:" + preMile);
            System.out.println("distance:" + distance);
            rxOkHttp3.PostVal(id, SVal, distance, TVal, PVal);
            //rxOkHttp3.displayVal(id,SVal,distance,TVal,PVal);
            BTShare.edit()
                    .putInt("preM", Mile)
                    .apply();
            //getFall();
            //System.out.println(number);
        });

    }

    void getID() {
        id = UserSetting.getString("id", null);
    }
    /**
     * Get AntiTheft Alert
     **/
    /*void getAntiTheft(){
        boolean notiFlag = UserSetting.getBoolean("noti", false);
        boolean PhFlag = UserSetting.getBoolean("ph", true);
        boolean ClFlag = UserSetting.getBoolean("cloud", false);
        if (PhFlag || !ClFlag) {
            System.out.println("NB is close will return");
            return;
        }
        if(!notiFlag || MuteFlag.Flag)return;
        getID();
        String AT;
        AT = rxOkHttp3.getATMsg(id);
        while (AT == null){
            System.out.println("ATMsg:"+AT);
            AT = rxOkHttp3.getATMsg(id);
        }
        System.out.println("ATMsg:"+AT);
        //Fall = "N";
        if(AT.equals("null")){
            System.out.println("AT null return");
            return;
        }
        if (AT.equals("Y")) {
            showNotification();
        }
        else System.out.println("AT = N return");
    }*/

    /**
     * Get Fall Alert
     **/
    protected void getFall() {
        boolean notiFlag = UserSetting.getBoolean("noti", false);
        boolean PhFlag = UserSetting.getBoolean("ph", true);
        boolean ClFlag = UserSetting.getBoolean("cloud", false);
        if (PhFlag || !ClFlag) {
            System.out.println("NB is close will return");
            return;
        }
        if (!notiFlag || MuteFlag.Flag) return;
        //all[0] = "AA";
        getID();
        final String[] Fall = new String[1];
        final String[] AT = new String[1];
        //all = rxOkHttp3.getFallMsg(id);
        rxOkHttp3.getFallMsg(id, new RxOkHttp3.FallCallback() {
            @Override
            public void onOkHttpResponse(String data) {
                all[0] = data;
                //if (!data.equals("AA")) Callback[0] = true;
                System.out.println("data:" + all[0]);

                Fall[0] = all[0].substring(0, 1);
                AT[0] = all[0].substring(1, 2);
               /* System.out.println("AllMsg:" + all[0]);
                System.out.println("Fall:" + Fall[0]);
                System.out.println("AT:" + AT[0]);*/
                //Fall = "N";
                /*if (Fall[0].equals("n") || AT[0].equals("u")) {
                    System.out.println("Fall null return");
                    //return;
                }*/
                if (Fall[0].equals("Y")) {
                    addData("Fall");
                    FallNotification();
                    System.out.println("Fall!");
                } //else System.out.println("Fall = N return");
                if (AT[0].equals("Y")) {
                    addData("AT");
                    ATNotification();
                    System.out.println("AT!");
                } //else System.out.println("AT = N return");
            }

            @Override
            public void onOkHttpFailure(Exception exception) {
                System.out.println("error:" + exception);
            }
        });

        //all[0] = "NN";

    }

    /**
     * FallMsg Stream
     **/
    public void FallListen() {
        boolean PhFlag = UserSetting.getBoolean("ph", true);
        boolean ClFlag = UserSetting.getBoolean("cloud", false);
        if (PhFlag || !ClFlag) {
            System.out.println("NB is close will return");
            return;
        }
        rxFallAlert.interval(3000, number -> {
            //Log.e("home_show_three", "======MainActivity======" + number);
            getFall();
            //getAntiTheft();
        });
    }

    protected void addData(String type) {
        Calendar mCal = Calendar.getInstance();
        CharSequence s = DateFormat.format("yyyy年MM月dd日 kk:mm:ss", mCal.getTime());
        CharSequence time = DateFormat.format("mm", mCal.getTime());
        String m = time.toString();
        int ATmin = Integer.parseInt(m);
        int Famin = Integer.parseInt(m);

        System.out.println(time.toString());
        if (type.equals("AT")) {
            if (ATmin == preATMin) {
                System.out.println("ATm Same will return");
                return;
            }
            preATMin = ATmin;
            ScanValue();
            FallData.edit()
                    .putInt("Fall1", 1)//0 無訊號/1 AT/2 Fall
                    .putString("Date1", s.toString())
                    .apply();
            System.out.println("added AT");
        } else if (type.equals("Fall")) {
            if (Famin == preFaMin) {
                System.out.println("ATm Same will return");
                return;
            }
            preFaMin = Famin;
            ScanValue();
            FallData.edit()
                    .putInt("Fall1", 2)//0 無訊號/1 AT/2 Fall
                    .putString("Date1", s.toString())
                    .apply();
            System.out.println("added Fall");
        }

    }

    private void ScanValue() {
        int i = 11;
        while (i > 0) {
            int Val;
            String index = Integer.toString(i);
            String FallIndex = "Fall" + index;
            String DateIndex = "Date" + index;
            //SharedPreferences a =  getPreferences(MODE_PRIVATE);
            Val = FallData.getInt(FallIndex, 0);
            //Val = FallData.getBoolean("11",false);
            if (Val != 0) {
                String next = "Fall" + (i + 1);
                String DateNext = "Date" + (i + 1);
                String date = FallData.getString(DateIndex, "");
                FallData.edit()
                        .putInt(next, Val)
                        .putString(DateNext, date)
                        .apply();
            }
            i--;
        }
    }

    /**
     * notification Anti-theft
     **/
    public void ATNotification() {
        Log.d(TAG, "showNotification: ");
        try {
            Calendar mCal = Calendar.getInstance();
            CharSequence s = DateFormat.format("MM月dd日 kk:mm:ss", mCal.getTime());
            Intent intent = new Intent(getApplicationContext(), com.Ray.Bicycle.Activity.Notification.class);
            intent.putExtra("noti_id", NOTIFY_REQUEST_ID);
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
            //Notification.Action action = new Notification.Action.Builder(0,"確定",pendingIntent).build();
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("您的腳踏車發生異常狀況")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("請立即前往確認\n" + s))
                    //.setContentText("請立即前往確認")
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
            int testNotifyId = 13;
            manager.notify(testNotifyId,
                    builder.build());
        } catch (Exception e) {

        }
    }

    /**
     * notification Fall Alert
     **/
    public void FallNotification() {
        Log.d(TAG, "FallNotification: ");
        try {
            Calendar mCal = Calendar.getInstance();
            CharSequence s = DateFormat.format("MM月dd日 kk:mm:ss", mCal.getTime());
            Intent intent = new Intent(getApplicationContext(), com.Ray.Bicycle.Activity.Notification.class);
            intent.putExtra("noti_id", NOTIFY_REQUEST_ID);
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
            //Notification.Action action = new Notification.Action.Builder(0,"確定",pendingIntent).build();
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("偵測到使用者跌倒")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("請立即前往確認\n" + s))
                    //.setContentText()
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